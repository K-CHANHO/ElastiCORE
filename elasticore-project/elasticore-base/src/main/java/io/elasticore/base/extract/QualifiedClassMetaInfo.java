package io.elasticore.base.extract;

import io.elasticore.base.model.ConstantParam;

public class QualifiedClassMetaInfo {

    public static final String TYPE_JAVA = "java";
    public static final String TYPE_PROTO = "proto";

    private String type;
    private String extensionName;
    private String fullClassName;
    private String simpleClassName;

    private QualifiedClassMetaInfo(String qualifiedClassName) {
        parse(qualifiedClassName);
    }

    public static QualifiedClassMetaInfo of(String qualifiedClassName) {
        return new QualifiedClassMetaInfo(qualifiedClassName);
    }

    /**
     * com.test.exam.TestClass
     * java://com.test.exam.TestClas2
     * proto://com.test.exam.TestClas2
     *
     * @param qualifiedClassName
     */
    private void parse(String qualifiedClassName) {
        if (qualifiedClassName == null || qualifiedClassName.trim().isEmpty()) {
            return;
        }

        String rawClassName = qualifiedClassName.trim();

        // Extract type (e.g., java://, proto://)
        int typeSepIndex = rawClassName.indexOf("://");
        if (typeSepIndex != -1) {
            this.type = rawClassName.substring(0, typeSepIndex);
            rawClassName = rawClassName.substring(typeSepIndex + 3); // Part after "://"
        } else {
            this.type = TYPE_JAVA;
        }

        this.extensionName = this.type;

        this.fullClassName = rawClassName;

        int lastDotIndex = rawClassName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < rawClassName.length() - 1) {
            this.simpleClassName = rawClassName.substring(lastDotIndex + 1);
        } else {
            this.simpleClassName = rawClassName;
        }
    }

    public String getType() {
        return type;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getSourcePath() {
        if(TYPE_PROTO.equals(type)) {
            return ConstantParam.PROPERTY_PROTO_SRC_HOME+"/"+ simpleClassName+ "." + extensionName;
        }

        return ConstantParam.PROPERTY_JAVA_SRC_HOME+"/"+ fullClassName.replace('.', '/') + "." + extensionName;
    }


}
