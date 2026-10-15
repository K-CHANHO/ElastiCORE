package io.elasticore.base.model.pub.rules;

import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The field asks one question and its items another, and a rule deciding
 * whether a collection can be searched or mapped is asking about the items.
 *
 * A List&lt;String&gt; is not itself a base type, so isBaseType() says no for
 * a collection of values and for a collection of entities alike. Reading a
 * rule off it drops List&lt;String&gt; from the search DTO, which is how this
 * distinction was found. These pin the two apart.
 */
public class FieldSpecItemTypeTest {

    private static class TestLoader extends AbstractModelLoader {
        Field parse(String name, String line) {
            return super.parseFieldLine(name, line, null);
        }
    }

    private final TestLoader loader = new TestLoader();

    private List<FieldSpec> fieldsOf(String... declarations) {
        EntitySpec e = new EntitySpec("Order");
        for (int i = 0; i < declarations.length; i += 2) {
            e.addField(loader.parse(declarations[i], declarations[i + 1]));
        }
        return e.fields();
    }

    @Test
    public void collectionOfValues_hasBaseItemsButIsNotOne() {
        FieldSpec tags = fieldsOf("tags", "List<String>").get(0);

        assertTrue(tags.isList(), "declared a list");
        assertTrue(tags.isBaseItemType(), "its items are values");
        assertFalse(tags.isBaseType(), "the list itself is not a base type");
    }

    @Test
    public void collectionOfEntities_hasNeither() {
        FieldSpec items = fieldsOf("items", "List<OrderItem>").get(0);

        assertTrue(items.isList(), "declared a list");
        assertFalse(items.isBaseItemType(), "its items are a model type");
        assertFalse(items.isBaseType(), "nor is the list");
    }

    /** A plain value answers both, so the two only diverge on collections. */
    @Test
    public void plainValue_answersBoth() {
        FieldSpec name = fieldsOf("name", "string").get(0);

        assertFalse(name.isList(), "not a list");
        assertTrue(name.isBaseType(), "the field is a value");
    }
}
