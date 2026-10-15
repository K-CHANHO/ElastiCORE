package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the v3 core service checks before it writes, stated at the rule level.
 *
 * The ddd sample drives these over HTTP (ApiContractTest); this pins the
 * text the rules emit, so a rule change that drops a guard fails here, next
 * to the rule, and not only in a sample module that may not be built.
 */
public class WriteGuardRulesTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec order(boolean versioned) {
        String base = "io.elasticore.blueprint.domain.shop";
        EntitySpec e = new EntitySpec("Order")
                .domain("shop", "Shop")
                .apiPath("/api/v1")
                .pkg("entity", base + ".entity")
                .pkg("dto", base + ".dto")
                .pkg("repository", base + ".repository")
                .pkg("service", base + ".service")
                .pkg("control", base + ".controller")
                .addField(loader.parse("orderId", "long @id @sequence"))
                .addField(loader.parse("orderNo", "string(30)"));
        if (versioned) e.addField(loader.parse("version", "long @jpa:Version"));
        return e;
    }

    private String render(EntitySpec e, String set, String target, SourceCode.TemplateSet templates) {
        return RuleCatalog.standard().engineFor(set).templateSet(templates).run(e, target).render();
    }

    private String service(EntitySpec e) {
        return render(e, "jpa-service-file", "service", SourceCode.TemplateSet.V3);
    }

    @Test
    public void aCreateCannotOverwrite() {
        String out = service(order(false));
        assertTrue(out.contains("if (dto.getOrderId() != null && helper.getOrder().existsById(dto.getOrderId()))"), out);
        assertTrue(out.contains("throw new DuplicateKeyException("), out);
    }

    @Test
    public void theVersionIsComparedWhereTheEntityHasOne() {
        String versioned = service(order(true));
        assertTrue(versioned.contains(
                "VersionCheck.check(dto, \"version\", entity.getVersion(), Order.class, dto.getOrderId());"), versioned);
        assertTrue(versioned.contains("entity.setVersion(null);"), "a create persists, never merges:\n" + versioned);

        String plain = service(order(false));
        assertFalse(plain.contains("VersionCheck"), plain);
        assertFalse(plain.contains("${"), "no slot left behind where the check is absent:\n" + plain);
    }

    @Test
    public void writesAreValidatedAndBulkPathsAreGone() {
        String out = service(order(false));
        assertTrue(out.contains("BeanValidation.validate(dto);"), out);
        assertTrue(out.contains("BeanValidation.validatePresent(dto);"), out);

        // Neither bulk path survives: each walks past cascade, listeners,
        // the version and the audit trail.
        assertFalse(out.contains(".delete(specification)"), out);
        assertFalse(out.contains("CriteriaUpdate"), out);
        assertFalse(out.contains("executeUpdate"), out);
        assertTrue(out.contains(".ifPresent(helper.getOrder()::delete)"), out);
    }

    @Test
    public void theCoreServiceIsNotABeanOfItsOwn() {
        String out = service(order(false));
        assertFalse(out.contains("@Service"), "the extension is the bean; two would be ambiguous:\n" + out);
        assertTrue(out.contains("public OrderCoreService(ShopRepositoryHelper helper) {"), out);
    }

    @Test
    public void aVersionIsNeverWrittenFromADto() {
        EntitySpec e = order(true);
        e.addField(loader.parse("lastModifiedBy",
                "string(50) @jpa:org.springframework.data.annotation.LastModifiedBy"));
        boolean version = false, audit = false, plain = false;
        for (FieldSpec f : e.fields()) {
            if (f.name().equals("version")) version = f.systemManaged() && f.isVersion();
            if (f.name().equals("lastModifiedBy")) audit = f.systemManaged() && !f.isVersion();
            if (f.name().equals("orderNo")) plain = !f.systemManaged();
        }
        assertTrue(version, "@jpa:Version is the persistence layer's");
        assertTrue(audit, "a Spring Data auditing field is the persistence layer's");
        assertTrue(plain, "an ordinary field is the request's to write");
    }
    @Test
    public void aPrimitiveKeyIsNeverComparedWithNull() {
        EntitySpec e = order(false);
        e.fields().clear();
        e.addField(loader.parse("orderId", "long @id @sequence @primitive"));
        e.addField(loader.parse("orderNo", "string(30)"));
        String out = service(e);
        assertFalse(out.contains("dto.getOrderId() != null"), "a long cannot be null: " + out);
        assertTrue(out.contains("if (true && "), out);
        assertTrue(out.contains("(dto.getOrderId() != 0)"), out);
    }

    @Test
    public void aPrimitiveKeyInKotlinIsALongComparedWithALongZero() {
        EntitySpec e = order(false);
        e.fields().clear();
        e.addField(loader.parse("orderId", "long @id @sequence @primitive"));
        e.addField(loader.parse("orderNo", "string(30)"));
        String out = render(e, "jpa-service-file", "service", SourceCode.TemplateSet.KOTLIN);
        assertFalse(out.contains("!= 0)"), "Kotlin refuses Long != Int: " + out);
        assertTrue(out.contains("(dto.orderId != null && dto.orderId != 0L)"), out);
        assertTrue(out.contains("if (dto.orderId != null && "), "the null check stays, or !! throws: " + out);
    }

    @Test
    public void aRootThatDeclaresAPromiseAlwaysGetsItsListener() {
        EntitySpec root = order(false).meta("invariant", "this is not a condition the grammar reads");
        java.util.List<ContributedArtifact> artifacts =
                new io.elasticore.base.model.pub.rules.ddd.DddInvariantRules().artifactsFor(root);
        assertTrue(artifacts.size() == 1 && artifacts.get(0).className().equals("OrderInvariants"),
                "a member's removal check names this class; it has to exist even when empty");
    }

    @Test
    public void aRootNothingServesGetsNoPorts() {
        EntitySpec unserved = order(false);
        assertTrue(new io.elasticore.base.model.pub.rules.ddd.DddRepositoryPortRules().artifactsFor(unserved).isEmpty(),
                "no Spring Data repository is generated for it, so an adapter would not compile");
        EntitySpec served = order(false).meta("expose", "10");
        assertTrue(new io.elasticore.base.model.pub.rules.ddd.DddRepositoryPortRules().artifactsFor(served).size() == 3);
    }
}
