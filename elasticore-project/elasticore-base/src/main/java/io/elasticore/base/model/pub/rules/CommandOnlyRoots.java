package io.elasticore.base.model.pub.rules;

/**
 * A rule set under which a root with nothing a request may edit has no
 * update endpoint: it is created, read, deleted and changed by its commands
 * (ddd-command).
 *
 * The runner asks for this rather than for the rule set by name, so that a
 * client generated for the same server - TypeScript - leaves out the update
 * the controller does not have.
 */
public interface CommandOnlyRoots {
}
