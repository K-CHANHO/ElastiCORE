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
 * In every v3 bundle, Java and Kotlin, DDD or not. Not in jpa-springboot,
 * whose mapper is 2.x's byte for byte.
 */
public class WriteGuardRules implements CodeRules {

    /** The mapper's reverse direction skips them. Before mapper-fields (20). */
    @Rule(value = "v3-mapper-system-managed", target = "mapper", order = 11)
    void mapperSkipsThem(EntitySpec e) {
        SourceCode c = ctx(e);
        for (FieldSpec f : e.allFields().isEmpty() ? e.fields() : e.allFields()) {
            if (f.systemManaged()) c.systemManaged(f.name());
        }
    }

    /**
     * And the DTO does not demand them: a declared-required createdBy would
     * otherwise refuse a create for leaving out the one value the mapper is
     * about to throw away. After dto-validation (20).
     */
    @Rule(value = "v3-dto-system-managed", target = "dto", order = 21)
    void dtoDoesNotRequireThem(EntitySpec e, FieldSpec f) {
        if (!f.systemManaged()) return;
        ctx(e).field(f).removeAnnotation("jakarta.validation.constraints.NotNull");
    }
}
