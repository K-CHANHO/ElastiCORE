package io.elasticore.base.model.pub.rules;

/**
 * A rule set under which another aggregate is referenced by its identity.
 *
 * The runner rewrites the model before any code is generated from it - a
 * to-one reference to another root becomes an id field that is written and a
 * read-only relation on the same column - and does so only where a rule set
 * that decides references says it takes them that way (ddd-reference).
 */
public interface ReferencesById {
}
