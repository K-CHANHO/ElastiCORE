package io.elasticore.base.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Listings come back in Windows' order everywhere: case-insensitive by name,
 * so a Linux or macOS build reads domains and model files in the same order.
 */
class FileOrderTest {

    @Test
    void namesSortCaseInsensitivelyAsOnWindows() {
        File[] files = {new File("b.yml"), new File("_shared.yml"), new File("A.yml"),
                new File("c.yml"), new File("B2.yml")};
        FileOrder.sorted(files);
        assertEquals(Arrays.asList("A.yml", "b.yml", "B2.yml", "c.yml", "_shared.yml"), names(files));
    }

    @Test
    void aListingIsSortedWhateverOrderTheFileSystemKeeps() throws IOException {
        File dir = Files.createTempDirectory("elcore-file-order").toFile();
        try {
            for (String name : new String[]{"shop", "billing", "Crm", "audit"}) {
                if (!new File(dir, name).mkdir()) {
                    throw new IOException("could not create " + name);
                }
            }
            assertEquals(Arrays.asList("audit", "billing", "Crm", "shop"), names(FileOrder.list(dir)));
        } finally {
            for (String name : new String[]{"shop", "billing", "Crm", "audit"}) {
                new File(dir, name).delete();
            }
            dir.delete();
        }
    }

    @Test
    void aMissingDirectoryListsAsNull() {
        assertNull(FileOrder.list(new File("no-such-directory-" + System.nanoTime())));
    }

    private static List<String> names(File[] files) {
        List<String> out = new ArrayList<String>();
        for (File f : files) {
            out.add(f.getName());
        }
        return out;
    }
}
