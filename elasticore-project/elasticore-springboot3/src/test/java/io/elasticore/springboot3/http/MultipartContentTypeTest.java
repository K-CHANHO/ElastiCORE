package io.elasticore.springboot3.http;

import io.elasticore.runtime.port.MultipartContentType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for multipart per-field content-type functionality.
 */
public class MultipartContentTypeTest {

    // Test interface with @MultipartContentType annotation
    public interface TestPortService {
        String testMethod(@MultipartContentType("application/json") TestRequest data, FileSystemResource file);
    }

    // Simple test DTO
    public static class TestRequest {
        private String name;
        private String value;

        public TestRequest() {}
        public TestRequest(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @Test
    public void testAnnotationDetection() throws Exception {
        // Get the method from interface
        Method method = TestPortService.class.getMethod("testMethod", TestRequest.class, FileSystemResource.class);
        Parameter[] params = method.getParameters();

        System.out.println("=== Annotation Detection Test ===");
        System.out.println("Method: " + method.getName());
        System.out.println("Parameter count: " + params.length);

        boolean foundMultipartContentType = false;
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            System.out.println("Param[" + i + "]: " + param.getName() +
                ", type=" + param.getType().getSimpleName() +
                ", hasMultipartContentType=" + param.isAnnotationPresent(MultipartContentType.class));

            if (param.isAnnotationPresent(MultipartContentType.class)) {
                MultipartContentType mct = param.getAnnotation(MultipartContentType.class);
                System.out.println("  @MultipartContentType value: " + mct.value());
                foundMultipartContentType = true;
                assertEquals("application/json", mct.value());
            }
        }

        assertTrue(foundMultipartContentType, "Should find @MultipartContentType annotation");
    }

    @Test
    public void testMultipartRequestCreation() {
        System.out.println("=== MultipartRequest Creation Test ===");

        // Simulate the flow in HttpServiceProxyFactory
        Map<String, Object> multipartParts = new LinkedHashMap<>();
        Map<String, String> multipartContentTypes = new HashMap<>();

        // Add parts
        TestRequest data = new TestRequest("test", "value");
        multipartParts.put("data", data);
        multipartContentTypes.put("data", "application/json");

        // Create a dummy file
        File tempFile = new File(System.getProperty("java.io.tmpdir"), "test.txt");
        FileSystemResource file = new FileSystemResource(tempFile);
        multipartParts.put("file", file);

        // Create MultipartRequest
        MultipartRequest mr = new MultipartRequest(multipartParts, multipartContentTypes);

        System.out.println("MultipartRequest created:");
        System.out.println("  Parts count: " + mr.getParts().size());
        System.out.println("  Has contentType for 'data': " + mr.hasContentType("data"));
        System.out.println("  ContentType for 'data': " + mr.getContentType("data"));
        System.out.println("  Has contentType for 'file': " + mr.hasContentType("file"));

        // Verify
        assertEquals(2, mr.getParts().size());
        assertTrue(mr.hasContentType("data"));
        assertEquals("application/json", mr.getContentType("data"));
        assertFalse(mr.hasContentType("file"));
    }

    @Test
    public void testInstanceOfCheck() {
        System.out.println("=== instanceof Check Test ===");

        Map<String, Object> parts = new LinkedHashMap<>();
        parts.put("data", new TestRequest("test", "value"));

        Map<String, String> contentTypes = new HashMap<>();
        contentTypes.put("data", "application/json");

        Object mainReqObject = new MultipartRequest(parts, contentTypes);

        System.out.println("mainReqObject class: " + mainReqObject.getClass().getName());
        System.out.println("Is MultipartRequest? " + (mainReqObject instanceof MultipartRequest));

        assertTrue(mainReqObject instanceof MultipartRequest,
            "mainReqObject should be instanceof MultipartRequest");
    }

    @Test
    public void testSimulatedFlow() throws Exception {
        System.out.println("=== Simulated HttpServiceProxyFactory Flow ===");

        // Simulate contentType check
        String contentType = "multipart/form-data";
        boolean isMultipartWithContentTypes = false;

        // Get method parameters
        Method method = TestPortService.class.getMethod("testMethod", TestRequest.class, FileSystemResource.class);
        Parameter[] methodParams = method.getParameters();

        System.out.println("ContentType: " + contentType);
        System.out.println("Contains multipart/form-data: " + contentType.toLowerCase().contains("multipart/form-data"));

        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            for (Parameter param : methodParams) {
                System.out.println("Checking param: " + param.getName() +
                    ", hasMultipartContentType: " + param.isAnnotationPresent(MultipartContentType.class));
                if (param.isAnnotationPresent(MultipartContentType.class)) {
                    isMultipartWithContentTypes = true;
                    break;
                }
            }
        }

        System.out.println("isMultipartWithContentTypes: " + isMultipartWithContentTypes);
        assertTrue(isMultipartWithContentTypes, "Should detect multipart with content types");

        // Simulate args processing
        Object[] args = new Object[]{
            new TestRequest("test", "value"),
            new FileSystemResource(new File(System.getProperty("java.io.tmpdir"), "test.txt"))
        };
        String[] paramNames = new String[]{"data", "file"};

        Map<String, Object> multipartParts = new LinkedHashMap<>();
        Map<String, String> multipartContentTypes = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String paramName = paramNames[i];

            // Check if primitive
            boolean isPrimitive = arg == null ||
                arg.getClass().isPrimitive() ||
                arg instanceof String ||
                arg instanceof Number ||
                arg instanceof Boolean;

            System.out.println("Param[" + i + "]: " + paramName +
                ", type=" + (arg != null ? arg.getClass().getSimpleName() : "null") +
                ", isPrimitive=" + isPrimitive);

            if (!isPrimitive) {
                multipartParts.put(paramName, arg);
                System.out.println("  Added to multipartParts: " + paramName);

                if (i < methodParams.length) {
                    MultipartContentType mct = methodParams[i].getAnnotation(MultipartContentType.class);
                    if (mct != null) {
                        multipartContentTypes.put(paramName, mct.value());
                        System.out.println("  Added contentType: " + mct.value());
                    }
                }
            }
        }

        // Create MultipartRequest
        Object mainReqObject = null;
        if (isMultipartWithContentTypes && !multipartParts.isEmpty()) {
            mainReqObject = new MultipartRequest(multipartParts, multipartContentTypes);
            System.out.println("Created MultipartRequest with " + multipartParts.size() + " parts");
        }

        System.out.println("mainReqObject type: " + (mainReqObject != null ? mainReqObject.getClass().getName() : "null"));
        System.out.println("Is MultipartRequest? " + (mainReqObject instanceof MultipartRequest));

        // Final assertions
        assertNotNull(mainReqObject);
        assertTrue(mainReqObject instanceof MultipartRequest);

        MultipartRequest mr = (MultipartRequest) mainReqObject;
        assertEquals(2, mr.getParts().size());
        assertTrue(mr.hasContentType("data"));
        assertEquals("application/json", mr.getContentType("data"));
    }
}
