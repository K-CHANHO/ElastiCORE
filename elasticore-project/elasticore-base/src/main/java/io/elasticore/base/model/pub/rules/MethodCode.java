package io.elasticore.base.model.pub.rules;

/**
 * A method of the file being generated, by name: a repository query, a port
 * operation, a service or controller method.
 *
 * What a rule puts on it is written above the method's declaration when the
 * file is written, so a rule need not know how the template lays the method
 * out. Every overload of the name gets it.
 *
 *   ctx(port).method("getUser").ensure(AnnoCode.of("io.micrometer.core.annotation.Timed"));
 */
public final class MethodCode {

    private final SourceCode code;
    private final String name;

    MethodCode(SourceCode code, String name) {
        this.code = code;
        this.name = name;
    }

    public String name() {
        return name;
    }

    /** Puts the annotation on the method unless one of the same type is there. */
    public MethodCode ensure(AnnoCode anno) {
        code.addMethodAnnotation(name, anno);
        return this;
    }
}
