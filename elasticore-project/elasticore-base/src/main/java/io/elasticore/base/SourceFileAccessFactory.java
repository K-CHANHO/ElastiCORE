package io.elasticore.base;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Defines a factory for creating {@link Writer} instances that are used to write source code.
 * This interface allows for flexible implementations of source code writers based on different
 * storage mechanisms or formats, such as files, databases, or in-memory storage.
 */
public interface SourceFileAccessFactory {

    /**
     * Creates and returns a {@link Writer} instance for writing source code associated with a
     * specified qualified class name. Implementations of this method are responsible for
     * determining the appropriate storage location and format for the source code, based on the
     * qualified class name provided.
     *
     * @param qualifiedClassName The fully qualified name of the class for which source code is to be written.
     *                           This name is used to determine the appropriate file name or storage identifier.
     * @return A {@link Writer} instance suitable for writing source code.
     * @throws IOException If an I/O error occurs during writer creation or if the storage mechanism
     *                     is not accessible or writable.
     */
    Writer getWriter(String qualifiedClassName) throws IOException;


    Reader getReader(String qualifiedClassName) throws IOException;


    boolean delete(String qualifiedClassName);

    String getFilePath(String qualifiedClassName);
}
