package io.elasticore.blueprint.ingest;

import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.runtime.DslLoader;
import io.elasticore.base.runtime.EntityConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end verification using the sample runtime DSL plus input rows shaped
 * like a monthly sales spreadsheet row (synthetic values).
 */
class EntityConverterTest {

    private static final String DSL_URI = "classpath:ingest-dsl/sales-monthly.yml";
    private static final String ENTITY_NAME = "SalesMonthly";

    private static Entity entity;

    @BeforeAll
    static void loadEntity() {
        DslLoader.getInstance().invalidateAll();
        ECoreModel model = DslLoader.getInstance().load(DSL_URI);
        entity = model.getEntityModels().findByName(ENTITY_NAME);
        assertThat(entity).as("DSL entity loaded").isNotNull();
    }

    @BeforeEach
    void resetDslCache() {
        // Some tests modify the cache; keep the entity handle but start fresh for loader tests.
    }

    @Test
    void convertsSalesRow() {
        // A spreadsheet row after developer-side Korean-header-to-DSL-field
        // normalization. All values are made up.
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("orderNo", "S00000000001");
        input.put("orderDate", "20260307");          // yyyyMMdd -> yyyy-MM-dd
        input.put("orderStatus", "\uC644\uB8CC");    // 완료 (raw passthrough; no @fn on this field)
        input.put("storeCd", "ST0001");
        input.put("qty", "3");
        input.put("storeNm", "Sample store");
        input.put("customerNm", "\uD64D\uAE38\uB3D9");  // placeholder name (Hong Gil-dong)
        input.put("productCd", "P0001");
        input.put("salesAmt", "60000");               // no comma
        input.put("discountAmt", "21,700");           // with comma - toNum must strip
        input.put("couponAmt", "0");
        input.put("pointAmt", "0");
        input.put("shippingFee", "3,000");

        List<Map<String, Object>> out = EntityConverter.toMap(entity, Collections.singletonList(input));

        assertThat(out).hasSize(1);
        Map<String, Object> row = out.get(0);

        // ID / string pass-through
        assertThat(row.get("orderNo")).isEqualTo("S00000000001");
        assertThat(row.get("storeCd")).isEqualTo("ST0001");
        assertThat(row.get("orderStatus")).isEqualTo("\uC644\uB8CC");

        // @fn(ymd)
        assertThat(row.get("orderDate")).isEqualTo("2026-03-07");

        // @fn(toNum) - handles both comma-free and comma-present input
        assertThat(row.get("salesAmt")).isEqualTo(new BigDecimal("60000"));
        assertThat(row.get("discountAmt")).isEqualTo(new BigDecimal("21700"));
        assertThat(row.get("shippingFee")).isEqualTo(new BigDecimal("3000"));

        // staticValues injected
        assertThat(row.get("payMethod")).isEqualTo("\uCE74\uB4DC");  // 카드
        assertThat(row.get("channelCd")).isEqualTo("X01");
        assertThat(row.get("settleType")).isEqualTo("MS");
    }

    @Test
    void convertsMultipleRowsPreservingOrder() {
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("orderNo", "A1");
        row1.put("discountAmt", "1,000");

        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("orderNo", "B2");
        row2.put("discountAmt", "2,500");

        List<Map<String, Object>> out = EntityConverter.toMap(entity, Arrays.asList(row1, row2));

        assertThat(out).hasSize(2);
        assertThat(out.get(0).get("orderNo")).isEqualTo("A1");
        assertThat(out.get(0).get("discountAmt")).isEqualTo(new BigDecimal("1000"));
        assertThat(out.get(1).get("orderNo")).isEqualTo("B2");
        assertThat(out.get(1).get("discountAmt")).isEqualTo(new BigDecimal("2500"));
    }

    @Test
    void staticValuesAppearOnEveryRow() {
        List<Map<String, Object>> out = EntityConverter.toMap(entity,
                Arrays.asList(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>()));
        assertThat(out).hasSize(3);
        for (Map<String, Object> r : out) {
            assertThat(r.get("channelCd")).isEqualTo("X01");
            assertThat(r.get("settleType")).isEqualTo("MS");
        }
    }

    @Test
    void emptySourceProducesEmptyResult() {
        assertThat(EntityConverter.toMap(entity, Collections.emptyList())).isEmpty();
        assertThat(EntityConverter.toMap(entity, (List<Map<String, Object>>) null)).isEmpty();
    }

    @Test
    void extractStaticValues_returnsAllThree() {
        Map<String, Object> sv = EntityConverter.extractStaticValues(entity);
        assertThat(sv)
                .containsEntry("channelCd", "X01")
                .containsEntry("settleType", "MS")
                .containsKey("payMethod");
    }
}
