package io.elasticore.springboot3.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The words an API error is said in, in the caller's language.
 *
 * A message is looked up by its code in, first to last:
 * <ol>
 *   <li>{@code messages[_locale].properties} on the classpath - the
 *       application's own translations, found even where Spring Boot did not
 *       set up a MessageSource;</li>
 *   <li>the domain's generated bundle, {@code i18n/<domain>-messages.properties},
 *       and a translation placed beside it ({@code ..._ko.properties});</li>
 *   <li>the application's MessageSource, for any other set-up it has.</li>
 * </ol>
 * Nothing found: the default the code was written with.
 *
 * The language is the request's Accept-Language. A request without one is
 * answered in the bundles' default wording - not in the language of the
 * machine the server runs on, which is what Spring would otherwise pick.
 */
public final class ErrorMessages {

    private final QuietBundles source = new QuietBundles();

    /**
     * @param application the application's MessageSource, or null
     * @param bundle      the domain's generated bundle, e.g. {@code i18n/shop-messages}
     */
    public ErrorMessages(MessageSource application, String bundle) {
        source.setBasenames("messages", bundle);
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setUseCodeAsDefaultMessage(false);
        if (application != null) source.setParentMessageSource(application);
    }

    /** The message for a code, or the default as given (not formatted) when no bundle has it. */
    public String text(String code, String defaultText, Object... args) {
        if (code == null) return defaultText;
        try {
            String found = source.getMessage(code, args, null, locale());
            return found != null ? found : defaultText;
        } catch (RuntimeException ex) {
            // A translation with a broken pattern must not turn a 4xx into a 500.
            return defaultText;
        }
    }

    /** A field error, a binding error: Spring's own codes (NotNull.customerDTO.login ... NotNull), then its default. */
    public String text(MessageSourceResolvable error) {
        try {
            return source.getMessage(error, locale());
        } catch (RuntimeException ex) {
            return error.getDefaultMessage();
        }
    }

    /**
     * The request's language, or the bundles' default (Locale.ROOT) when the
     * request names none: Spring's resolver would answer the server
     * machine's locale for that request.
     */
    static Locale locale() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
                String header = request.getHeader("Accept-Language");
                Locale resolved = LocaleContextHolder.getLocale();
                // No header, and the resolver answered the container's fallback
                // (the machine's locale): nobody chose it. An application that
                // chose otherwise - a session, a cookie, a fixed locale - is kept.
                if ((header == null || header.trim().isEmpty()) && resolved.equals(request.getLocale())) {
                    return Locale.ROOT;
                }
            }
        } catch (RuntimeException | LinkageError ignore) {
            // Not a servlet request (or no servlet API): the resolver's answer stands.
        }
        return LocaleContextHolder.getLocale();
    }

    /**
     * A bundle that is not there is remembered as not there. Spring caches
     * only the bundles it finds and logs a warning on every lookup of one it
     * does not - for an application without messages.properties, a warning
     * on every error response.
     */
    private static final class QuietBundles extends ResourceBundleMessageSource {
        private final Map<String, ResourceBundle> found = new ConcurrentHashMap<>();
        private final Set<String> missing = ConcurrentHashMap.newKeySet();

        @Override
        protected ResourceBundle getResourceBundle(String basename, Locale locale) {
            String key = basename + '\u0000' + locale;
            ResourceBundle bundle = found.get(key);
            if (bundle != null) return bundle;
            if (missing.contains(key)) return null;
            try {
                bundle = doGetBundle(basename, locale);
                found.put(key, bundle);
                return bundle;
            } catch (MissingResourceException ex) {
                missing.add(key);
                return null;
            }
        }
    }
}
