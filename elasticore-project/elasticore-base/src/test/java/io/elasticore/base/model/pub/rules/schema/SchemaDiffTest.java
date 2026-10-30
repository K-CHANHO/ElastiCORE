package io.elasticore.base.model.pub.rules.schema;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** What a change of one column becomes: run at once when it only widens, waiting for review when it can lose data. */
public class SchemaDiffTest {

    private static Schema of(Column... columns) {
        Table t = new Table("item", "Item");
        t.column(Column.of("item_id", ColumnType.BIGINT).nullable(false));
        for (Column c : columns) t.column(c);
        t.primaryKey("item_id");
        Schema s = new Schema();
        s.table(t);
        return s;
    }

    private static SchemaDiff.Change only(Schema before, Schema after) {
        List<SchemaDiff.Change> changes = SchemaDiff.between(before, after);
        assertEquals(1, changes.size(), changes.toString());
        return changes.get(0);
    }

    @Test
    public void aLongerTextOrMoreDigitsRunAtOnce() {
        assertTrue(only(of(Column.of("name", ColumnType.TEXT).length(40)),
                of(Column.of("name", ColumnType.TEXT).length(80))).safe);
        assertTrue(only(of(Column.of("price", ColumnType.DECIMAL).decimal(10, 2)),
                of(Column.of("price", ColumnType.DECIMAL).decimal(14, 2))).safe);
    }

    @Test
    public void aDecimalThatSaidNoneNarrowsWhenItSaysLess() {
        // No precision is DECIMAL(38,2): a declared (12,2) is narrower.
        SchemaDiff.Change c = only(of(Column.of("price", ColumnType.DECIMAL)),
                of(Column.of("price", ColumnType.DECIMAL).decimal(12, 2)));
        assertFalse(c.safe, c.description);
        // And fewer digits after the point, with as many in all, is narrower too.
        assertFalse(only(of(Column.of("rate", ColumnType.DECIMAL).decimal(12, 4)),
                of(Column.of("rate", ColumnType.DECIMAL).decimal(12, 2))).safe);
    }

    @Test
    public void aTextGrowingPastOraclesInlineLimitIsATypeChange() {
        // VARCHAR2(4000 CHAR) -> CLOB: no ALTER makes one of the other.
        SchemaDiff.Change c = only(of(Column.of("memo", ColumnType.TEXT).length(2000)),
                of(Column.of("memo", ColumnType.TEXT).length(8000)));
        assertFalse(c.safe, c.description);
    }

    @Test
    public void sqlServerKeepsTheColumnsNullabilityAndItsIndexes() {
        Schema before = of(Column.of("code", ColumnType.TEXT).length(10).nullable(false));
        before.table("item").index(new Table.Index("uk_item_code", Collections.singletonList("code"), true));
        Schema after = of(Column.of("code", ColumnType.TEXT).length(20).nullable(false));
        after.table("item").index(new Table.Index("uk_item_code", Collections.singletonList("code"), true));
        SchemaDiff.Change c = only(before, after);
        assertTrue(c.safe, c.description);
        List<String> sql = c.sql(Database.SQLSERVER);
        assertEquals("DROP INDEX uk_item_code ON item", sql.get(0));
        assertEquals("ALTER TABLE item ALTER COLUMN code NVARCHAR(20) NOT NULL", sql.get(1));
        assertTrue(sql.get(2).startsWith("CREATE UNIQUE INDEX uk_item_code ON item (code)"), sql.get(2));
        // A column that becomes required: its type change keeps it nullable; the NOT NULL waits for review.
        List<SchemaDiff.Change> both = SchemaDiff.between(of(Column.of("note", ColumnType.TEXT).length(10)),
                of(Column.of("note", ColumnType.TEXT).length(20).nullable(false)));
        assertEquals(2, both.size(), both.toString());
        assertEquals("ALTER TABLE item ALTER COLUMN note NVARCHAR(20) NULL", both.get(0).sql(Database.SQLSERVER).get(0));
        assertTrue(both.get(0).safe);
        assertFalse(both.get(1).safe);
    }

    @Test
    public void anIndexByANameTakenOverIsNotDroppedAndMadeAgainAsWritten() {
        // A history of the project's own made the index, and the state took its name over (-PddlBaseline):
        // the database may hold it by another name, or as a constraint.
        Schema before = of(Column.of("code", ColumnType.TEXT).length(10).nullable(false));
        before.table("item").index(new Table.Index("uk_item_code", Collections.singletonList("code"), true));
        before.takeOverKeysAndIndexes();
        Schema after = of(Column.of("code", ColumnType.TEXT).length(20).nullable(false));
        after.table("item").index(new Table.Index("uk_item_code", Collections.singletonList("code"), true));

        // Widening the column runs at once where no history is for SQL Server - no index is touched there.
        assertTrue(only(before, after).safe, only(before, after).description);
        // On SQL Server the widening drops the index and makes it again: it waits.
        before.databases().add(Database.SQLSERVER.id);
        SchemaDiff.Change around = only(before, after);
        assertFalse(around.safe);
        assertTrue(around.description.contains("uk_item_code") && around.description.contains("-PddlBaseline"),
                around.description);

        // The index itself changes: dropped and made again by that name, both wait.
        Schema moved = of(Column.of("code", ColumnType.TEXT).length(10).nullable(false));
        moved.table("item").index(new Table.Index("uk_item_code", java.util.Arrays.asList("code", "item_id"), true));
        List<SchemaDiff.Change> changes = SchemaDiff.between(before, moved);
        assertEquals(2, changes.size(), changes.toString());
        for (SchemaDiff.Change c : changes) assertFalse(c.safe, c.description);
        // Without a name taken over, the same change runs as written.
        Schema plain = of(Column.of("code", ColumnType.TEXT).length(10).nullable(false));
        plain.table("item").index(new Table.Index("uk_item_code", Collections.singletonList("code"), true));
        for (SchemaDiff.Change c : SchemaDiff.between(plain, moved)) assertTrue(c.safe, c.description);
    }
}
