package io.elasticore.blueprint.ingest.sample;

import io.elasticore.base.model.ECoreModel;
import io.elasticore.base.model.entity.Entity;
import io.elasticore.base.runtime.DslLoader;
import io.elasticore.base.runtime.EntityConverter;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reference sample - NOT part of ElastiCORE.
 *
 * Shows the end-to-end pipeline a service would use to ingest
 * a monthly sales Excel. The Excel-parsing half is a
 * placeholder because Apache POI (or any Excel library) is the developer's
 * responsibility; ElastiCORE never touches xlsx bytes directly.
 *
 * Pipeline:
 *   1. Developer's Excel loader (placeholder here)   -> List<Map<KoreanHeader, Object>>
 *   2. KoreanHeaderNormalizer.normalize()            -> List<Map<DslField, Object>>
 *   3. DslLoader + EntityConverter.toMap()           -> List<Map<TargetField, Object>>
 *   4. Developer injects per-upload metadata         -> ready for persistence
 *   5. Developer's batch insert (JPA or native)      -> rows in SALES_MONTH
 *
 * This class has no JUnit @Test methods so it is never executed by
 * `./gradlew test`; its only job is to compile and read clearly.
 */
public final class SalesIngestSample {

    private static final String DSL_URI = "classpath:ingest-dsl/sales-monthly.yml";
    private static final String DSL_ENTITY = "SalesMonthly";

    private SalesIngestSample() {
    }

    /**
     * Hypothetical ingest entry point. Returns target-keyed rows, ready for
     * persistence by the caller.
     *
     * @param xlsxStream  the raw .xlsx input stream (developer's responsibility)
     * @param salesYm     매출년월 (e.g. "202603") derived from filename
     * @param tenantId    테넌트 ID resolved from auth context
     */
    public static List<Map<String, Object>> runIngest(InputStream xlsxStream,
                                                      String salesYm,
                                                      String tenantId) {
        // 1) Developer's Excel loader. Replace with Apache POI XSSFWorkbook (or
        //    poi-ooxml streaming reader for large files). This sample returns
        //    one made-up row so the stub compiles and has no runtime dependency.
        List<Map<String, Object>> excelRows = developerExcelLoader(xlsxStream);

        // 2) Normalize Korean headers to DSL field names.
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> r : excelRows) {
            normalized.add(KoreanHeaderNormalizer.normalize(r));
        }

        // 3) Load DSL (cached) and convert with EntityConverter.
        ECoreModel model = DslLoader.getInstance().load(DSL_URI);
        Entity entity = model.getEntityModels().findByName(DSL_ENTITY);
        if (entity == null) {
            throw new IllegalStateException("Runtime DSL entity not found: " + DSL_ENTITY);
        }
        List<Map<String, Object>> targetRows = EntityConverter.toMap(entity, normalized);

        // 4) Inject per-upload metadata that is not in the DSL staticValues
        //    block (values that change per request).
        LocalDateTime now = LocalDateTime.now();
        for (Map<String, Object> row : targetRows) {
            row.put("tenantId", tenantId);
            row.put("salesYm", salesYm);
            row.put("fileCd", "FILE_0001");    // derived from filename
            row.put("createdBy", "SYSTEM");    // replace with auth principal
            row.put("createdAt", now);
        }

        // 5) Persistence is caller's concern for Phase 1. In Phase 2 this will
        //    be handled by DbTransactionGateway.batchInsert(...).
        return targetRows;
    }

    /**
     * Placeholder for the developer-owned Excel reader. Replace with an
     * Apache POI based implementation:
     *
     * <pre>{@code
     * try (Workbook wb = new XSSFWorkbook(xlsxStream)) {
     *     Sheet sheet = wb.getSheet("SALES_" + salesYm);
     *     Row headerRow = sheet.getRow(0);
     *     List<String> headers = extractHeaders(headerRow);
     *     List<Map<String, Object>> rows = new ArrayList<>();
     *     for (int r = 1; r <= sheet.getLastRowNum(); r++) {
     *         Row row = sheet.getRow(r);
     *         if (row == null) continue;
     *         Map<String, Object> rowMap = new LinkedHashMap<>();
     *         for (int c = 0; c < headers.size(); c++) {
     *             rowMap.put(headers.get(c), cellValue(row.getCell(c)));
     *         }
     *         rows.add(rowMap);
     *     }
     *     return rows;
     * }
     * }</pre>
     */
    private static List<Map<String, Object>> developerExcelLoader(InputStream xlsxStream) {
        // Pretend the loader returned one row (made-up values).
        // Keys are Korean headers the developer saw in the actual Excel.
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("\uC8FC\uBB38\uBC88\uD638", "S00000000001");                    // 주문번호
        row.put("\uC8FC\uBB38\uC77C\uC790", "20260307");                        // 주문일자
        row.put("\uC8FC\uBB38\uC0C1\uD0DC", "\uC644\uB8CC");                            // 주문상태 = 완료
        row.put("\uD310\uB9E4\uAE08\uC561", "60000");                           // 판매금액
        row.put("\uD560\uC778\uAE08\uC561", "21,700");                          // 할인금액
        // xlsxStream intentionally unused in the stub
        if (xlsxStream != null) {
            // close quietly if caller gave us a real stream
            try { xlsxStream.close(); } catch (Exception ignored) { }
        }
        return Collections.singletonList(row);
    }
}
