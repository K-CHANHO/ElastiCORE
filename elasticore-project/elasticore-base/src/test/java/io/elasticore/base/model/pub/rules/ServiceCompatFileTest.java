package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The 2.x-compatible CoreService: service.tmpl filled the way
 * ServiceSrcPublisher fills it. The byte-for-byte check against the committed
 * tree is elcoreTreeCheck; these pin the branches the blueprint may not reach.
 */
public class ServiceCompatFileTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String fieldLine) {
            return super.parseFieldLine(name, fieldLine, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private EntitySpec base(String name) {
        String pkg = "io.elasticore.blueprint.domain.shop";
        return new EntitySpec(name)
                .knownEntity("Order")
                .knownEntity("Customer")
                .domain("shop", "Shop")
                .config("j2ee", "jakarta")
                .pkg("entity", pkg + ".entity")
                .pkg("dto", pkg + ".dto")
                .pkg("repository", pkg + ".repository")
                .pkg("service", pkg + ".service");
    }

    private SourceCode run(EntitySpec e) {
        return RuleCatalog.standard()
                .engineFor("jpa-service-compat")
                .templateSet(SourceCode.TemplateSet.COMPATIBLE)
                .run(e, "service");
    }

    @Test
    public void exposedEntity_rendersExposeShapeAndJoinFetch() {
        EntitySpec e = base("Order")
                .meta("expose", null)
                .addField(loader.parse("orderId", "long @id @sequence"))
                .addField(loader.parse("customer", "Customer @ManyToOne @fetchjoin"));
        SourceCode code = run(e);
        String out = code.render();

        assertTrue(out.startsWith("package io.elasticore.blueprint.domain.shop.service;"), out);
        assertTrue(out.contains("import jakarta.persistence.EntityNotFoundException;"), out);
        assertTrue(out.contains("public class OrderCoreService {"), out);
        assertTrue(out.contains("protected final ShopRepositoryHelper helper;"), out);
        assertTrue(out.contains("root.fetch(\"customer\", JoinType.LEFT);"), out);
        assertTrue(out.contains("public List<OrderDTO> listBySearch(OrderSrchDTO searchDTO)"), out);
        assertTrue(out.contains("public Page<OrderDTO> findBySearch(OrderSrchDTO searchDTO)"), out);
        assertTrue(out.contains("ShopMapper.mapping(dto, entity, true, helper.getEntityManager());"), out);
        assertTrue(out.contains("return helper.getOrder().findById(orderId)"), out);
        assertFalse(out.contains("${"), out);
        // Only the slots 2.x leaves empty here are left empty: the extra
        // imports of a single key, and one 2.x never fills at all.
        java.util.List<String> unfilled = code.unfilledSlots();
        unfilled.removeAll(java.util.Arrays.asList("importList", "joinAnnotatedFetches"));
        assertTrue(unfilled.isEmpty(), "misspelt or missing slots: " + unfilled);
    }

    @Test
    public void plainEntity_rendersListShapeWithoutJoinFetch() {
        EntitySpec e = base("Customer")
                .addField(loader.parse("customerId", "long @id"))
                .addField(loader.parse("name", "string(50)"));
        String out = run(e).render();

        assertTrue(out.contains("public List<CustomerDTO> findBySearch(CustomerSrchDTO searchDTO)"), out);
        assertFalse(out.contains("listBySearch"), out);
        assertFalse(out.contains("query.distinct(true);"), out);
    }

    @Test
    public void compositeKey_isWrappedInItsIdentityAndImported() {
        EntitySpec e = base("Order")
                .meta("service", null)
                .addField(loader.parse("orderId", "long @id"))
                .addField(loader.parse("lineNo", "int @id"));
        String out = run(e).render();

        assertTrue(out.contains("import io.elasticore.blueprint.domain.shop.entity.OrderIdentity;"), out);
        assertTrue(out.contains("public boolean deleteById(Long orderId ,Integer lineNo)"), out);
        assertTrue(out.contains("deleteById(new OrderIdentity(orderId ,lineNo));"), out);
    }
}
