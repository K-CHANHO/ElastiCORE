package io.elasticore.base.model;

/**
 * Enumeration of component types used in the ElastiCORE framework.
 * Each type represents a different kind of model component with its name and suffix.
 */
public enum ComponentType {

    /** Entity component type */
    ENTITY("entity",""),
    /** Data Transfer Object component type */
    DTO("dto","DTO"),
    /** Repository component type */
    REPOSITORY("repository","Repository"),

    /** Field component type */
    FIELD("field",""),
    /** Method component type */
    METHOD("method",""),

    /** Entity group component type */
    ENTITY_GROUP("entityGrp",""),
    /** DTO group component type */
    DTO_GROUP("dtoGrp",""),

    /** Enumeration component type */
    ENUM("enumeration",""),
    /** Enumeration group component type */
    ENUM_GROUP("enumGrp",""),

    /** Port adapter component type */
    PORT("port","PortAdapter"),
    /** Port group component type */
    PORT_GROUP("portGrp",""),

    /** Input/Output DTO component type */
    IO("io",""), //dto
    /** Service component type */
    SERVICE("service","CoreService");

    private final String name;

    private final String suffix;

    ComponentType(String name,String suffix) {
        this.name = name;
        this.suffix = suffix;
    }

    /**
     * Gets the name of this component type.
     * @return the component type name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the suffix used for this component type.
     * @return the component type suffix
     */
    public String getSuffix() {
        return suffix;
    }
}
