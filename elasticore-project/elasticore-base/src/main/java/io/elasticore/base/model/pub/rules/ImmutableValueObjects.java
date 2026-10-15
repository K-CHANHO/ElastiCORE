package io.elasticore.base.model.pub.rules;

/**
 * Marks a rule set that makes @embeddable types immutable value objects.
 *
 * Where one is selected, a value object may travel whole through a DTO and be
 * shared by the DTO and the entity: nobody can change it in place. Where none
 * is, an embeddable is a mutable bean, and handing the same instance to both
 * sides would let a change to the DTO reach the managed entity - so it stays
 * out of the DTO, as in 2.x.
 */
public interface ImmutableValueObjects {
}
