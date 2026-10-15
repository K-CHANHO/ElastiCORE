package io.elasticore.blueprint.ingest.sample;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reference sample - NOT part of ElastiCORE.
 *
 * Converts a Map keyed by Korean Excel headers into a Map keyed by DSL field
 * names expected by the runtime ingest pipeline (`EntityConverter.toMap`).
 *
 * Developers maintain this table themselves; ElastiCORE does not know (and
 * intentionally does not need to know) about Excel headers or their Korean
 * names. The alias table below handles header-name drift between the canonical
 * mapping definition and the actual Excel file. All headers are made up.
 *
 * Not executed by ./gradlew test - provided so new developers can see how the
 * normalization layer is supposed to fit in front of EntityConverter.
 */
public final class KoreanHeaderNormalizer {

    private static final Map<String, String> HEADER_TO_FIELD = new HashMap<>();

    static {
        // Order identity
        put("\uC8FC\uBB38\uBC88\uD638", "orderNo");                          // 주문번호

        // Dates / status
        put("\uC8FC\uBB38\uC77C\uC790", "orderDate");                        // 주문일자 (File 1 header)
        put("\uC8FC\uBB38\uC77C", "orderDate");                          // 주문일 (File 2 canonical)
        put("\uC8FC\uBB38\uC0C1\uD0DC", "orderStatus");                      // 주문상태
        put("\uCC98\uB9AC\uC0C1\uD0DC", "orderStatus");                      // 처리상태 (canonical)

        // Store
        put("\uB9E4\uC7A5\uCF54\uB4DC", "storeCd");                          // 매장코드
        put("\uB9E4\uC7A5\uBA85", "storeNm");                            // 매장명
        put("\uC9C0\uC810\uBA85", "storeNm");                            // 지점명 (alias)

        // Customer / product
        put("\uACE0\uAC1D\uBA85", "customerNm");                         // 고객명
        put("\uAD6C\uB9E4\uC790", "customerNm");                         // 구매자 (canonical)
        put("\uC0C1\uD488\uCF54\uB4DC", "productCd");                        // 상품코드
        put("\uC0C1\uD488\uBA85", "productNm");                          // 상품명
        put("\uBD84\uB958\uCF54\uB4DC", "categoryCd");                       // 분류코드

        // Quantities / amounts
        put("\uC218\uB7C9", "qty");                                  // 수량
        put("\uBC18\uD488\uC218\uB7C9", "returnQty");                        // 반품수량
        put("\uB2E8\uAC00", "unitPrice");                            // 단가
        put("\uD310\uB9E4\uAE08\uC561", "salesAmt");                         // 판매금액
        put("\uB9E4\uCD9C\uC561", "salesAmt");                           // 매출액 (alias)
        put("\uD560\uC778\uAE08\uC561", "discountAmt");                      // 할인금액
        put("\uCFE0\uD3F0\uAE08\uC561", "couponAmt");                        // 쿠폰금액
        put("\uD3EC\uC778\uD2B8\uC0AC\uC6A9\uC561", "pointAmt");                     // 포인트사용액
        put("\uBC30\uC1A1\uBE44", "shippingFee");                        // 배송비
        put("\uBD80\uAC00\uC138", "taxAmt");                             // 부가세
        put("\uC21C\uB9E4\uCD9C", "netAmt");                             // 순매출
        put("\uC218\uC218\uB8CC\uC728", "feeRate");                          // 수수료율
        put("\uC218\uC218\uB8CC", "feeAmt");                             // 수수료

        // Notes
        put("\uBE44\uACE0", "memo");                                 // 비고
    }

    private KoreanHeaderNormalizer() {
    }

    private static void put(String header, String field) {
        HEADER_TO_FIELD.put(header, field);
    }

    /**
     * Convert one row from Korean-header-keyed to DSL-field-keyed.
     * Unknown headers are silently dropped (developer discretion).
     */
    public static Map<String, Object> normalize(Map<String, Object> excelRow) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (excelRow == null) return out;
        for (Map.Entry<String, Object> e : excelRow.entrySet()) {
            String h = e.getKey();
            if (h == null) continue;
            String dslField = HEADER_TO_FIELD.get(h.trim());
            if (dslField != null) out.put(dslField, e.getValue());
        }
        return out;
    }
}
