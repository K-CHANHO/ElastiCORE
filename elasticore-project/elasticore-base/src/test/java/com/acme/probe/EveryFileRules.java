package com.acme.probe;

import io.elasticore.base.model.pub.rules.AnnoCode;
import io.elasticore.base.model.pub.rules.CodeRules;
import io.elasticore.base.model.pub.rules.EntitySpec;
import io.elasticore.base.model.pub.rules.EnumSpec;
import io.elasticore.base.model.pub.rules.FieldSpec;
import io.elasticore.base.model.pub.rules.ModelCheck;
import io.elasticore.base.model.pub.rules.PortSpec;
import io.elasticore.base.model.pub.rules.QuerySpec;
import io.elasticore.base.model.pub.rules.Rule;

import static io.elasticore.base.model.pub.rules.Ctx.ctx;

/** A project's rules for every kind of file the DSL describes, as the rule guide writes them. */
public class EveryFileRules implements CodeRules {

    @Rule(value = "every-internal", target = "dto")
    void internal(EntitySpec dto, FieldSpec field) {
        if (field.hasDsl("internal")) {
            ctx(dto).field(field).merge(AnnoCode.of("io.swagger.v3.oas.annotations.media.Schema").val("hidden", true));
        }
    }

    @Rule(value = "every-like-min", target = "srchdto")
    void likeMin(EntitySpec search, FieldSpec field) {
        if ("like".equals(field.dslValue("search"))) {
            ctx(search).field(field).ensure(AnnoCode.of("jakarta.validation.constraints.Size").val("min", 2));
        }
    }

    @Rule(value = "every-queries", target = "repository")
    void queries(EntitySpec entity) {
        for (QuerySpec query : entity.queries()) {
            if (query.sql() == null) continue;
            if (query.sql().toLowerCase().replaceAll("\\s+", " ").contains("select *")) {
                ModelCheck.fail(entity.getName() + "." + query.name() + ": name the columns instead of SELECT *");
            }
            if (query.isSelect()) {
                ctx(entity).method(query.name()).ensure(
                        AnnoCode.of("org.springframework.transaction.annotation.Transactional").val("readOnly", true));
            }
        }
    }

    @Rule(value = "every-external-call", target = "port")
    void externalCall(PortSpec port) {
        ctx(port).ensureClassAnnotation(AnnoCode.of("com.acme.erp.support.ExternalSystem").value(port.getName()));
        for (PortSpec.Operation op : port.operations()) {
            if (op.url() != null) {
                ctx(port).method(op.name()).ensure(AnnoCode.of("com.acme.erp.support.ExternalCall")
                        .value(op.httpMethod() + " " + op.url()));
            }
        }
    }

    @Rule(value = "every-enum-schema", target = "enum")
    void enumSchema(EnumSpec enumeration) {
        ctx(enumeration).ensureClassAnnotation(AnnoCode.of("io.swagger.v3.oas.annotations.media.Schema")
                .val("enumAsRef", true));
    }
}
