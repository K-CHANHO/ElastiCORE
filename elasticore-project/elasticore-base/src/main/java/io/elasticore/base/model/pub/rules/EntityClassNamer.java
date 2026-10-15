package io.elasticore.base.model.pub.rules;

/**
 * A rule set that writes an entity's regenerated file under another name.
 *
 * The runner writes the entity file itself, so a rule set that moves it -
 * ddd-behavior writes the state as OrderBase and hands the name Order to a
 * class the developer owns - says so here, and the runner asks every
 * configured set without learning which one answered.
 */
public interface EntityClassNamer {

    /** The class name for this entity's regenerated file, or null to leave it as it is. */
    String entityClassName(EntitySpec spec);
}
