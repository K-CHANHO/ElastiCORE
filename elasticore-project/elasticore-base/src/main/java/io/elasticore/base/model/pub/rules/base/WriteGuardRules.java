package io.elasticore.base.model.pub.rules.base;

import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.Rule;
import io.elasticore.base.model.pub.rules.SourceCode;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/**
 * The fields the persistence layer writes, kept out of the request's reach.
 *
 * The optimistic-lock version and Spring Data's auditing columns are read out
 * to the DTO - a client needs the version to send it back - but never written
 * from one. A request that could set them could forge who changed a row and
 * when, and a version copied onto a loaded row is ignored by Hibernate, so the
 * lock the entity declares would not be taken; the service compares it
 * explicitly instead (VersionCheck).
 *
 * A field the DSL marks @readonly (alias @noinput) is kept out the same way:
 * the server sets it - a service's beforeSave, a root's own method - and the
 * response shows it. Jackson already drops the value a request sends; the
 * mapper now also drops one that server code put on the DTO before a save,
 * so the field is never taken from a DTO. The mark counts on either side: on
 * the entity's field (every DTO of the entity) or on the DTO's own field
 * (that DTO only).
 *
 * In every v3 bundle, Java and Kotlin, DDD or not. Not in jpa-springboot,
 * whose mapper is 2.x's byte for byte.
 */
public class WriteGuardRules implements CodeRules {

    /**
     * The mapper's reverse direction skips them. Before mapper-fields (20),
     * and before ddd-behavior-mapper (12): the reverse pass asks this first,
     * so a sealed field of a split root that is also @readonly is not written
     * through the creation writer either.
     */
    @Rule(value = "v3-mapper-system-managed", target = "mapper", order = 11)
    void mapperSkipsThem(EntitySpec e) {
        SourceCode c = ctx(e);
        for (FieldSpec f : e.allFields().isEmpty() ? e.fields() : e.allFields()) {
            if (f.systemManaged() || serverSet(f)) c.systemManaged(f.name());
        }
        for (FieldSpec f : e.dtoFields()) {
            if (serverSet(f)) c.systemManaged(f.name());
        }
    }

    /**
     * Whether the DSL says the server, not a request, sets this field:
     * @readonly or @noinput on a field that is not the identity. The identity
     * keeps its write: "@id @genid @readonly" only hides a generated key from
     * input, a create already resets one the database generates, and a DTO
     * turned into an entity is told apart from a new row by its key.
     */
    public static boolean serverSet(FieldSpec f) {
        return f.hasDslAny("readonly", "noinput") && !f.hasDslAny("id", "pk");
    }

    /**
     * And the DTO does not demand them: a declared-required createdBy would
     * otherwise refuse a create for leaving out the one value the mapper is
     * about to throw away. A required @readonly field likewise: a request
     * cannot send it, so a @NotNull or @NotBlank on it would refuse every
     * create. The column stays NOT NULL; the server fills it. After
     * dto-validation (20).
     */
    @Rule(value = "v3-dto-system-managed", target = "dto", order = 21)
    void dtoDoesNotRequireThem(EntitySpec e, FieldSpec f) {
        if (f.systemManaged()) {
            ctx(e).field(f).removeAnnotation("jakarta.validation.constraints.NotNull");
        }
        if (serverSet(f)) {
            ctx(e).field(f).removeAnnotation("jakarta.validation.constraints.NotNull")
                    .removeAnnotation("jakarta.validation.constraints.NotBlank");
        }
    }
}
