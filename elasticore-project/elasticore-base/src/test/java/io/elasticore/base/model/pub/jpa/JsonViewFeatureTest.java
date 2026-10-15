package io.elasticore.base.model.pub.jpa;

import io.elasticore.base.model.MetaInfo;
import io.elasticore.base.model.DataModelComponent;
import io.elasticore.base.model.core.Annotation;
import io.elasticore.base.model.entity.Field;
import io.elasticore.base.model.loader.module.AbstractModelLoader;
import io.elasticore.base.util.CodeTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for the ElastiCORE JsonView feature.
 * Tests annotation parsing, code generation, auto-inference, and edge cases.
 */
class JsonViewFeatureTest {

    // ========================================================================
    // Inner helper: TestModelLoader for parsing DSL field strings
    // ========================================================================
    private static class TestModelLoader extends AbstractModelLoader {
        @Override
        public Field parseFieldLine(String fieldNm, String fieldLine) {
            return super.parseFieldLine(fieldNm, fieldLine, null);
        }

        @Override
        public Map<String, Annotation> loadAnnotationMap(String fieldLine) {
            return super.loadAnnotationMap(fieldLine);
        }
    }

    private TestModelLoader loader;

    @BeforeEach
    void setUp() {
        loader = new TestModelLoader();
    }

    // ========================================================================
    // Group 1: @view Annotation Parsing Tests
    // ========================================================================
    @Nested
    @DisplayName("Group 1: @view Annotation Parsing from DSL")
    class ViewAnnotationParsingTests {

        @Test
        @DisplayName("@view(cru) should parse with value 'cru'")
        void viewCruParsing() {
            Map<String, Annotation> annotations = loader.loadAnnotationMap("string @view(cru)");

            assertTrue(annotations.containsKey("view"), "Should contain 'view' annotation");
            assertEquals("cru", annotations.get("view").getValue());
        }

        @Test
        @DisplayName("@view(r) should parse with value 'r'")
        void viewReadOnlyParsing() {
            Map<String, Annotation> annotations = loader.loadAnnotationMap("string @view(r)");

            assertTrue(annotations.containsKey("view"));
            assertEquals("r", annotations.get("view").getValue());
        }

        @Test
        @DisplayName("@view(create,update,read) should parse comma-separated value")
        void viewCommaSeparatedParsing() {
            Map<String, Annotation> annotations = loader.loadAnnotationMap("string @view(create,update,read)");

            assertTrue(annotations.containsKey("view"));
            assertEquals("create,update,read", annotations.get("view").getValue());
        }

        @Test
        @DisplayName("@id and @view(ur) should both be present on same field")
        void multipleAnnotationsWithView() {
            Map<String, Annotation> annotations = loader.loadAnnotationMap("long @id @view(ur)");

            assertTrue(annotations.containsKey("id"), "Should contain 'id' annotation");
            assertTrue(annotations.containsKey("view"), "Should contain 'view' annotation");
            assertEquals("ur", annotations.get("view").getValue());
        }

        @Test
        @DisplayName("@view(cr) should parse with value 'cr'")
        void viewCreateReadParsing() {
            Map<String, Annotation> annotations = loader.loadAnnotationMap("string @view(cr)");

            assertTrue(annotations.containsKey("view"));
            assertEquals("cr", annotations.get("view").getValue());
        }

        @Test
        @DisplayName("parseFieldLine should produce Field with @view annotation")
        void parseFieldLineWithView() {
            Field field = loader.parseFieldLine("username", "string @view(cru)");

            assertTrue(field.hasAnnotation("view"));
            assertEquals("cru", field.getAnnotationValue("view"));
        }

        @Test
        @DisplayName("parseFieldLine with @id @genid @view(r)")
        void parseFieldLineWithIdGenidAndView() {
            Field field = loader.parseFieldLine("id", "long @id @genid @view(r)");

            assertTrue(field.hasAnnotation("id"));
            assertTrue(field.hasAnnotation("genid"));
            assertTrue(field.hasAnnotation("view"));
            assertEquals("r", field.getAnnotationValue("view"));
        }
    }

    // ========================================================================
    // Group 2: generateJsonViewAnnotation() Tests (via reflection)
    // ========================================================================
    @Nested
    @DisplayName("Group 2: generateJsonViewAnnotation via reflection")
    class GenerateJsonViewAnnotationTests {

        private Method generateMethod;
        private SrcFilePublisher publisherInstance;

        @BeforeEach
        void setUp() throws Exception {
            // Use reflection to access private method
            generateMethod = SrcFilePublisher.class.getDeclaredMethod(
                    "generateJsonViewAnnotation", String.class, CodeTemplate.Paragraph.class);
            generateMethod.setAccessible(true);

            // Create instance with null publisher - generateJsonViewAnnotation does not use it
            publisherInstance = new SrcFilePublisher(null);
        }

        @Test
        @DisplayName("'cru' generates @JsonView with all three view classes")
        void cruGeneratesAllViews() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "cru", p);

            assertTrue(result, "Should return true for valid view value");
            assertEquals(1, p.size(), "Should add exactly one @JsonView line");
            String line = p.toString(0);
            assertTrue(line.contains("@JsonView"));
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertTrue(line.contains("{"), "Multiple views should use braces");
        }

        @Test
        @DisplayName("'r' generates single @JsonView without braces")
        void singleViewNoBraces() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "r", p);

            assertTrue(result);
            assertEquals(1, p.size());
            String line = p.toString(0);
            assertEquals("@JsonView(Views.Read.class)", line);
        }

        @Test
        @DisplayName("'cr' generates @JsonView with Create and Read")
        void createReadViews() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "cr", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertFalse(line.contains("Views.Update.class"));
            assertTrue(line.contains("{"), "Two views should use braces");
        }

        @Test
        @DisplayName("'u' generates single @JsonView(Views.Update.class)")
        void singleUpdateView() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "u", p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Update.class)", p.toString(0));
        }

        @Test
        @DisplayName("'c' generates single @JsonView(Views.Create.class)")
        void singleCreateView() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "c", p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Create.class)", p.toString(0));
        }

        @Test
        @DisplayName("Comma-separated 'create,update' generates correct views")
        void commaSeparatedCreateUpdate() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "create,update", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertFalse(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Comma-separated 'create' (single item) generates without braces")
        void commaSeparatedSingleItem() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "create", p);

            assertTrue(result);
            // 'create' does not contain comma, so single-char mode: 'c','r','e','a','t','e'
            // Only 'c', 'r' are valid chars, so 'create' in single-char mode yields Create + Read
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Comma-separated 'create,update,read' generates all three views")
        void commaSeparatedAllThree() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "create,update,read", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertTrue(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Empty view value returns false")
        void emptyViewReturnsfalse() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "", p);

            assertFalse(result, "Empty view value should return false");
            assertEquals(0, p.size(), "No annotation should be added");
        }

        @Test
        @DisplayName("Unknown characters 'xyz' returns false")
        void unknownCharsReturnFalse() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "xyz", p);

            assertFalse(result, "Unknown characters should return false");
            assertEquals(0, p.size());
        }

        @Test
        @DisplayName("Unknown comma-separated values return false")
        void unknownCommaSeparatedReturnFalse() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "foo,bar", p);

            assertFalse(result, "Unknown comma-separated values should return false");
            assertEquals(0, p.size());
        }
    }

    // ========================================================================
    // Group 3: inferJsonViewFromAnnotations() Tests (via reflection)
    // ========================================================================
    @Nested
    @DisplayName("Group 3: inferJsonViewFromAnnotations - auto mode inference")
    class InferJsonViewFromAnnotationsTests {

        private Method inferMethod;
        private SrcFilePublisher publisherInstance;

        @BeforeEach
        void setUp() throws Exception {
            inferMethod = SrcFilePublisher.class.getDeclaredMethod(
                    "inferJsonViewFromAnnotations", Field.class, CodeTemplate.Paragraph.class);
            inferMethod.setAccessible(true);
            publisherInstance = new SrcFilePublisher(null);
        }

        private Field buildFieldWithAnnotations(String name, String type, String... annotationNames) {
            Map<String, Annotation> annotationMap = new HashMap<>();
            for (String ann : annotationNames) {
                annotationMap.put(ann.toLowerCase(), Annotation.create(ann.toLowerCase()));
            }
            return Field.builder()
                    .name(name)
                    .type(type)
                    .annotationMap(annotationMap)
                    .build();
        }

        private Field buildFieldWithAnnotationValue(String name, String type,
                                                     String annName, String annValue) {
            Map<String, Annotation> annotationMap = new HashMap<>();
            annotationMap.put(annName.toLowerCase(), Annotation.create(annName.toLowerCase(), annValue));
            return Field.builder()
                    .name(name)
                    .type(type)
                    .annotationMap(annotationMap)
                    .build();
        }

        @Test
        @DisplayName("@id @genid: exclude from create, keep update+read")
        void idWithGenidExcludesCreate() throws Exception {
            Field field = buildFieldWithAnnotations("id", "long", "id", "genid");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result, "Should generate @JsonView for auto-generated ID");
            String line = p.toString(0);
            assertTrue(line.contains("Views.Update.class"), "Auto-gen ID should be in Update view");
            assertTrue(line.contains("Views.Read.class"), "Auto-gen ID should be in Read view");
            assertFalse(line.contains("Views.Create.class"), "Auto-gen ID should NOT be in Create view");
        }

        @Test
        @DisplayName("@id @sequence: exclude from create, keep update+read")
        void idWithSequenceExcludesCreate() throws Exception {
            Field field = buildFieldWithAnnotations("id", "long", "id", "sequence");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Update.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertFalse(line.contains("Views.Create.class"));
        }

        @Test
        @DisplayName("@id only (no genid/sequence): all views, no @JsonView generated")
        void idOnlyNoJsonView() throws Exception {
            Field field = buildFieldWithAnnotations("id", "long", "id");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertFalse(result, "@id alone means all views visible, no @JsonView needed");
            assertEquals(0, p.size());
        }

        @Test
        @DisplayName("@updatable(false): exclude from update view")
        void updatableFalseExcludesUpdate() throws Exception {
            Map<String, Annotation> annotationMap = new HashMap<>();
            annotationMap.put("updatable", Annotation.create("updatable", "false"));
            Field field = Field.builder()
                    .name("createdAt")
                    .type("string")
                    .annotationMap(annotationMap)
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertFalse(line.contains("Views.Update.class"));
        }

        @Test
        @DisplayName("jpa:createddate: read only")
        void createdDateIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("createdDate", "datetime", "jpa:createddate");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("jpa:lastmodifieddate: read only")
        void lastModifiedDateIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("updatedDate", "datetime", "jpa:lastmodifieddate");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("jpa:createdby: read only")
        void createdByIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("createdBy", "string", "jpa:createdby");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("jpa:lastmodifiedby: read only")
        void lastModifiedByIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("modifiedBy", "string", "jpa:lastmodifiedby");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("jpa:version: read only")
        void versionIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("version", "long", "jpa:version");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@formula: read only")
        void formulaIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("computed", "string", "formula");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@readonly: read only")
        void readonlyIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("status", "string", "readonly");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@noinput: read only (alias for readonly)")
        void noinputIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("status", "string", "noinput");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@writeonly: create+update only (no read)")
        void writeonlyIsCreateAndUpdateOnly() throws Exception {
            Field field = buildFieldWithAnnotations("password", "string", "writeonly");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertFalse(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("@nooutput: create+update only (alias for writeonly)")
        void nooutputIsCreateAndUpdateOnly() throws Exception {
            Field field = buildFieldWithAnnotations("secret", "string", "nooutput");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertFalse(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Normal field (no annotations): no @JsonView generated")
        void normalFieldNoJsonView() throws Exception {
            Field field = Field.builder()
                    .name("username")
                    .type("string")
                    .annotationMap(new HashMap<>())
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertFalse(result, "Normal field should not have @JsonView");
            assertEquals(0, p.size());
        }

        @Test
        @DisplayName("@id @genid with @readonly: readonly takes precedence, read only")
        void idGenidWithReadonlyIsReadOnly() throws Exception {
            Field field = buildFieldWithAnnotations("id", "long", "id", "genid", "readonly");
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();

            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }
    }

    // ========================================================================
    // Group 4: @view Dual Syntax Detailed Verification
    // ========================================================================
    @Nested
    @DisplayName("Group 4: @view dual syntax detailed output verification")
    class ViewDualSyntaxTests {

        private Method generateMethod;
        private SrcFilePublisher publisherInstance;

        @BeforeEach
        void setUp() throws Exception {
            generateMethod = SrcFilePublisher.class.getDeclaredMethod(
                    "generateJsonViewAnnotation", String.class, CodeTemplate.Paragraph.class);
            generateMethod.setAccessible(true);
            publisherInstance = new SrcFilePublisher(null);
        }

        @Test
        @DisplayName("'cru' output has braces and all three view classes")
        void cruOutputFormat() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "cru", p);

            String line = p.toString(0);
            // Verify exact format: @JsonView({Views.Create.class, Views.Read.class, Views.Update.class})
            assertTrue(line.startsWith("@JsonView({"));
            assertTrue(line.endsWith("})"));
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertTrue(line.contains("Views.Update.class"));
        }

        @Test
        @DisplayName("'r' output has no braces")
        void singleViewNoBraces() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "r", p);

            String line = p.toString(0);
            assertEquals("@JsonView(Views.Read.class)", line);
            assertFalse(line.contains("{"), "Single view should not have braces");
        }

        @Test
        @DisplayName("'cr' output has braces with Create and Read")
        void createReadOutputFormat() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "cr", p);

            String line = p.toString(0);
            assertTrue(line.startsWith("@JsonView({"));
            assertTrue(line.endsWith("})"));
        }

        @Test
        @DisplayName("'cu' output has braces with Create and Update")
        void createUpdateOutputFormat() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "cu", p);

            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertFalse(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Comma-separated 'create,update' produces correct output")
        void commaSeparatedCreateUpdateOutput() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "create,update", p);

            String line = p.toString(0);
            assertTrue(line.startsWith("@JsonView({"));
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertFalse(line.contains("Views.Read.class"));
        }

        @Test
        @DisplayName("Comma-separated 'read' (single name) produces single-char mode result")
        void commaSeparatedSingleRead() throws Exception {
            // 'read' has no comma, so single-char mode: 'r','e','a','d' -> only 'r' maps
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "read", p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("Uppercase 'CRU' is normalized to lowercase")
        void uppercaseNormalized() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "CRU", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Read.class"));
            assertTrue(line.contains("Views.Update.class"));
        }

        @Test
        @DisplayName("Mixed case 'Create,Update' comma-separated is normalized")
        void mixedCaseCommaSeparated() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "Create,Update", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
        }

        @Test
        @DisplayName("Spaces around comma-separated values are trimmed")
        void spacesAreTrimmed() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "create , update , read", p);

            assertTrue(result);
            String line = p.toString(0);
            assertTrue(line.contains("Views.Create.class"));
            assertTrue(line.contains("Views.Update.class"));
            assertTrue(line.contains("Views.Read.class"));
        }
    }

    // ========================================================================
    // Group 5: Edge Cases
    // ========================================================================
    @Nested
    @DisplayName("Group 5: Edge cases and annotation priority")
    class EdgeCaseTests {

        private Method inferMethod;
        private Method generateMethod;
        private SrcFilePublisher publisherInstance;

        @BeforeEach
        void setUp() throws Exception {
            inferMethod = SrcFilePublisher.class.getDeclaredMethod(
                    "inferJsonViewFromAnnotations", Field.class, CodeTemplate.Paragraph.class);
            inferMethod.setAccessible(true);

            generateMethod = SrcFilePublisher.class.getDeclaredMethod(
                    "generateJsonViewAnnotation", String.class, CodeTemplate.Paragraph.class);
            generateMethod.setAccessible(true);

            publisherInstance = new SrcFilePublisher(null);
        }

        @Test
        @DisplayName("Explicit @view(r) overrides auto-inference for @id @genid field")
        void explicitViewOverridesAutoInference() {
            // Build field with @id @genid @view(r)
            Map<String, Annotation> annotationMap = new HashMap<>();
            annotationMap.put("id", Annotation.create("id"));
            annotationMap.put("genid", Annotation.create("genid"));
            annotationMap.put("view", Annotation.create("view", "r"));

            Field field = Field.builder()
                    .name("id")
                    .type("long")
                    .annotationMap(annotationMap)
                    .build();

            // The field has @view(r), so setJsonViewInfo should use generateJsonViewAnnotation
            // and NOT inferJsonViewFromAnnotations
            String viewValue = field.getAnnotationValue("view");
            assertNotNull(viewValue, "Field should have @view annotation value");
            assertEquals("r", viewValue);

            // Verify generate produces read-only view
            try {
                CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
                boolean result = (boolean) generateMethod.invoke(publisherInstance, viewValue, p);
                assertTrue(result);
                assertEquals("@JsonView(Views.Read.class)", p.toString(0));
            } catch (Exception e) {
                fail("Reflection invocation failed: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Field with @readonly and @view(r): @view takes priority in setJsonViewInfo flow")
        void viewAnnotationWithReadonly() {
            // When @view is present, it is always used regardless of other annotations
            Map<String, Annotation> annotationMap = new HashMap<>();
            annotationMap.put("readonly", Annotation.create("readonly"));
            annotationMap.put("view", Annotation.create("view", "r"));

            Field field = Field.builder()
                    .name("status")
                    .type("string")
                    .annotationMap(annotationMap)
                    .build();

            assertEquals("r", field.getAnnotationValue("view"));

            try {
                CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
                boolean result = (boolean) generateMethod.invoke(publisherInstance, "r", p);
                assertTrue(result);
                assertEquals("@JsonView(Views.Read.class)", p.toString(0));
            } catch (Exception e) {
                fail("Reflection invocation failed: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("Duplicate chars in @view(rrr) should produce single Read view")
        void duplicateCharsProduceSingleView() throws Exception {
            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) generateMethod.invoke(publisherInstance, "rrr", p);

            assertTrue(result);
            // LinkedHashSet deduplicates, so only one Views.Read.class
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@view(rc) is same as @view(cr) - order preserved by LinkedHashSet insertion order")
        void reverseOrderView() throws Exception {
            CodeTemplate.Paragraph pRC = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "rc", pRC);

            CodeTemplate.Paragraph pCR = CodeTemplate.newParagraph();
            generateMethod.invoke(publisherInstance, "cr", pCR);

            // Both should contain same view classes (order may differ based on char iteration)
            String lineRC = pRC.toString(0);
            String lineCR = pCR.toString(0);

            assertTrue(lineRC.contains("Views.Read.class"));
            assertTrue(lineRC.contains("Views.Create.class"));
            assertTrue(lineCR.contains("Views.Read.class"));
            assertTrue(lineCR.contains("Views.Create.class"));
        }

        @Test
        @DisplayName("All server-managed annotations combined: still read only")
        void allServerManagedCombined() throws Exception {
            Field field = Field.builder()
                    .name("audit")
                    .type("string")
                    .annotationMap(new HashMap<String, Annotation>() {{
                        put("jpa:createddate", Annotation.create("jpa:createddate"));
                        put("jpa:version", Annotation.create("jpa:version"));
                        put("readonly", Annotation.create("readonly"));
                    }})
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Read.class)", p.toString(0));
        }

        @Test
        @DisplayName("@writeonly with @id @genid: create=false, update=true, read=false")
        void writeonlyWithAutoGenId() throws Exception {
            // @id @genid -> create=false
            // @writeonly -> read=false
            // Result: update only
            Field field = Field.builder()
                    .name("token")
                    .type("string")
                    .annotationMap(new HashMap<String, Annotation>() {{
                        put("id", Annotation.create("id"));
                        put("genid", Annotation.create("genid"));
                        put("writeonly", Annotation.create("writeonly"));
                    }})
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertTrue(result);
            assertEquals("@JsonView(Views.Update.class)", p.toString(0));
        }

        @Test
        @DisplayName("Null annotation map: normal field, no @JsonView")
        void nullAnnotationMapNoJsonView() throws Exception {
            Field field = Field.builder()
                    .name("name")
                    .type("string")
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertFalse(result, "Field with null annotations should not generate @JsonView");
        }

        @Test
        @DisplayName("@readonly + @writeonly combined: all false, no @JsonView generated")
        void readonlyAndWriteonly() throws Exception {
            // readonly: create=false, update=false
            // writeonly: read=false
            // Result: all false -> edge case, skip
            Field field = Field.builder()
                    .name("contradiction")
                    .type("string")
                    .annotationMap(new HashMap<String, Annotation>() {{
                        put("readonly", Annotation.create("readonly"));
                        put("writeonly", Annotation.create("writeonly"));
                    }})
                    .build();

            CodeTemplate.Paragraph p = CodeTemplate.newParagraph();
            boolean result = (boolean) inferMethod.invoke(publisherInstance, field, p);

            assertFalse(result, "All-false edge case should not generate @JsonView");
            assertEquals(0, p.size());
        }
    }
}
