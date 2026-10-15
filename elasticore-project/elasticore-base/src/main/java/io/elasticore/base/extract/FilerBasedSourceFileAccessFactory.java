package io.elasticore.base.extract;

import io.elasticore.base.SourceFileAccessFactory;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class FilerBasedSourceFileAccessFactory implements SourceFileAccessFactory {

    private Filer filer;

    public FilerBasedSourceFileAccessFactory(Filer filer) {
        this.filer = filer;
    }

    @Override
    public Writer getWriter(String qualifiedClassName) throws IOException {
        Writer writer = this.filer.createSourceFile(qualifiedClassName).openWriter();
        String ls = System.lineSeparator();
        if ("\n".equals(ls)) {
            return writer;
        }
        return new java.io.FilterWriter(writer) {
            @Override
            public void write(String str, int off, int len) throws IOException {
                String s = str.substring(off, off + len);
                s = s.replace("\r\n", "\n").replace("\n", ls);
                super.write(s, 0, s.length());
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                write(new String(cbuf, off, len), 0, len);
            }

            @Override
            public void write(int c) throws IOException {
                if (c == '\n') {
                    super.write(ls, 0, ls.length());
                } else {
                    super.write(c);
                }
            }
        };
    }

    @Override
    public Reader getReader(String qualifiedClassName) throws IOException {
        boolean ignoreEncodingErrors = true;
        return this.filer.createSourceFile(qualifiedClassName).openReader(ignoreEncodingErrors);
    }

    @Override
    public boolean delete(String qualifiedClassName) {
        try {
            return this.filer.createSourceFile(qualifiedClassName).delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getFilePath(String qualifiedClassName) {
        return qualifiedClassName;
    }
}
