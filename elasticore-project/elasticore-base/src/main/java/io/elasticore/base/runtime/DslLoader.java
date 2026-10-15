package io.elasticore.base.runtime;

import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.loader.ModelLoaderContext;
import io.elasticore.base.model.loader.module.YamlFileMainModelLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Runtime loader for ElastiCORE DSL YAML files.
 *
 * Supported URI forms:
 *   - /abs/path/to.yml or relative/path.yml        (local filesystem)
 *   - classpath:ingest-dsl/foo.yml                  (ClassLoader resource)
 *   - http://host/foo.yml, https://host/foo.yml     (remote, follows redirects)
 *
 * Loaded models are cached with a configurable TTL (default 10 minutes).
 * The cache key is the URI as supplied. Use {@link #invalidate(String)} to force
 * reload after an external DSL change.
 *
 * This class exists so that application code can do:
 *
 *     ECoreModel model = DslLoader.getInstance().load("classpath:ingest-dsl/sales-monthly.yml");
 *     Entity entity = model.getEntityModels().getItem("SalesMonthly");
 *     List<Map<String,Object>> rows = EntityConverter.toMap(entity, inputRows);
 *
 * The loader never touches the build-time blueprint directory.
 */
public final class DslLoader {

    private static final DslLoader INSTANCE = new DslLoader();

    /** Default cache entry lifetime (milliseconds). */
    public static final long DEFAULT_TTL_MILLIS = 10L * 60L * 1000L;

    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<String, CacheEntry>();
    private volatile long ttlMillis = DEFAULT_TTL_MILLIS;

    public static DslLoader getInstance() {
        return INSTANCE;
    }

    DslLoader() {
        // package-private constructor; tests may subclass / instantiate if needed.
    }

    /** Override cache TTL (milliseconds). Existing entries keep their original expiry. */
    public void setTtlMillis(long ttlMillis) {
        if (ttlMillis < 0) throw new IllegalArgumentException("ttlMillis must be >= 0");
        this.ttlMillis = ttlMillis;
    }

    public long getTtlMillis() {
        return this.ttlMillis;
    }

    /**
     * Load an ECoreModel from the given URI, returning a cached copy when possible.
     */
    public ECoreModel load(String uri) {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("uri must not be null/empty");
        }
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(uri);
        if (cached != null && now < cached.expiresAt) {
            return cached.model;
        }
        ECoreModel model = doLoad(uri);
        cache.put(uri, new CacheEntry(model, now + ttlMillis));
        return model;
    }

    /** Remove a single URI from the cache. */
    public void invalidate(String uri) {
        if (uri != null) cache.remove(uri);
    }

    /** Clear the entire cache. */
    public void invalidateAll() {
        cache.clear();
    }

    /** Number of currently cached entries (unexpired count not tracked). Useful in tests. */
    public int cacheSize() {
        return cache.size();
    }

    // --- internal ---

    private ECoreModel doLoad(String uri) {
        File yaml = resolveToFile(uri);
        String parent = yaml.getParent();
        if (parent == null) parent = ".";

        ModelLoaderContext ctx = ModelLoaderContext.createForRuntime("runtime", parent);
        YamlFileMainModelLoader loader = new YamlFileMainModelLoader(yaml.getAbsolutePath());
        try {
            return loader.load(ctx);
        } catch (RuntimeException re) {
            throw new RuntimeException("Failed to load DSL from " + uri + ": " + re.getMessage(), re);
        }
    }

    private File resolveToFile(String uri) {
        if (uri.startsWith("classpath:")) {
            String resource = uri.substring("classpath:".length());
            if (resource.startsWith("/")) resource = resource.substring(1);
            return copyClasspathToTemp(resource);
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return downloadToTemp(uri);
        }
        File f = new File(uri);
        if (!f.exists()) {
            throw new IllegalArgumentException("DSL file not found: " + uri);
        }
        return f;
    }

    private File copyClasspathToTemp(String resource) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = DslLoader.class.getClassLoader();
        InputStream in = cl.getResourceAsStream(resource);
        if (in == null) {
            throw new IllegalArgumentException("classpath resource not found: " + resource);
        }
        try {
            Path tmp = Files.createTempFile("elcore-dsl-", ".yml");
            try (InputStream src = in;
                 FileOutputStream out = new FileOutputStream(tmp.toFile())) {
                copy(src, out);
            }
            File f = tmp.toFile();
            f.deleteOnExit();
            return f;
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to stage classpath DSL: " + resource, ioe);
        }
    }

    private File downloadToTemp(String url) {
        try {
            Path tmp = Files.createTempFile("elcore-dsl-", ".yml");
            try (InputStream src = openFollowingRedirects(url);
                 FileOutputStream out = new FileOutputStream(tmp.toFile())) {
                copy(src, out);
            }
            File f = tmp.toFile();
            f.deleteOnExit();
            return f;
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to download DSL: " + url, ioe);
        }
    }

    private InputStream openFollowingRedirects(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(30_000);
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == 307 || status == 308) {
            String next = conn.getHeaderField("Location");
            if (next == null) throw new IOException("Redirect with no Location header: " + url);
            return openFollowingRedirects(next);
        }
        return conn.getInputStream();
    }

    private static void copy(InputStream in, FileOutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
    }

    /**
     * Utility: read a URI's raw bytes (used in tests and diagnostics).
     */
    public static byte[] readBytes(String uri) {
        try {
            if (uri.startsWith("classpath:")) {
                String res = uri.substring("classpath:".length());
                if (res.startsWith("/")) res = res.substring(1);
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) cl = DslLoader.class.getClassLoader();
                try (InputStream in = cl.getResourceAsStream(res);
                     ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    if (in == null) throw new IllegalArgumentException("classpath resource not found: " + res);
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
                    return bos.toByteArray();
                }
            }
            return Files.readAllBytes(new File(uri).toPath());
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to read " + uri, ioe);
        }
    }

    private static final class CacheEntry {
        final ECoreModel model;
        final long expiresAt;
        CacheEntry(ECoreModel model, long expiresAt) {
            this.model = model;
            this.expiresAt = expiresAt;
        }
    }
}
