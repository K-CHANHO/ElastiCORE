package io.elasticore.base.model.relation;

/**
 * Enumeration of relationship types used in the ElastiCORE framework.
 * Each type represents a different kind of relationship between model components.
 */
public enum RelationType {

    /** Child relationship type */
    CHILD("child"),
    /** Rollup relationship type */
    ROLLUP("rollup"),

    /** Rolldown relationship type */
    ROLLDOWN("rolldown"),
    /** Template relationship type */
    TEMPLATE("template"),
    /** Template-to relationship type */
    TEMPLATE_TO("templateTo"),
    /** One-to-one relationship type */
    ONE_TO_ONE("ontToOne"),
    /** One-to-many relationship type */
    ONE_TO_MANY("oneToMany"),
    /** Many-to-one relationship type */
    MANY_TO_ONE("manyToOne"),
    /** Many-to-many relationship type */
    MANY_TO_MANY("manyToMany"),
    /** Searched relationship type */
    SEARCHED("searched"),
    /** Searchable relationship type */
    SEARCHABLE("searchable"),
    /** Embedded relationship type */
    EMBEDDED("embedded"),
    /** Embeddable relationship type */
    EMBEDDABLE("embeddable"),

    /** Association relationship type */
    AOSSOCIATION("association"),

    /** Foreign key reference (no object relationship, metadata only) */
    FK_REFERENCE("fkReference"),

    /** Search result relationship type */
    SEARCH_RESULT("searchResult"),

    /** Mapper relationship type */
    MAPPER("mapper"),
    /** Super (inheritance) relationship type */
    SUPER("super");


    private String name;

    private RelationType(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this relationship type.
     * @return the relationship type name
     */
    public String getName() {
        return this.name;
    }
}
