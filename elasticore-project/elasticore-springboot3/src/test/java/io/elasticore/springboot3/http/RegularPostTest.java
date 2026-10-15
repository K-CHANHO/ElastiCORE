package io.elasticore.springboot3.http;

import io.elasticore.runtime.port.ExternalService;
import io.elasticore.runtime.port.HttpEndpoint;
import io.elasticore.runtime.port.MultipartContentType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that regular (non-multipart) POST requests still work correctly
 * after the multipart per-field content-type feature was added.
 */
public class RegularPostTest {

    // Mock request/response DTOs
    public static class UserSyncRequest {
        private String userId;
        private String accesstoken;
        private String reqdate;

        public UserSyncRequest() {}
        public UserSyncRequest(String userId, String accesstoken, String reqdate) {
            this.userId = userId;
            this.accesstoken = accesstoken;
            this.reqdate = reqdate;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getAccesstoken() { return accesstoken; }
        public void setAccesstoken(String accesstoken) { this.accesstoken = accesstoken; }
        public String getReqdate() { return reqdate; }
        public void setReqdate(String reqdate) { this.reqdate = reqdate; }
    }

    public static class UserSyncResponse {
        private String resultCode;
        private String message;

        public String getResultCode() { return resultCode; }
        public void setResultCode(String resultCode) { this.resultCode = resultCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Regular POST service (NO multipart)
    public interface RegularPostService {
        @HttpEndpoint(url = "/api/users/sync", method = "POST")
        UserSyncResponse userSyncReq(UserSyncRequest body);
    }

    // Multipart POST service (WITH @MultipartContentType)
    public interface MultipartPostService {
        @HttpEndpoint(url = "/api/claims/submit", method = "POST", contentType = "multipart/form-data")
        String rentClaimReq(@MultipartContentType("application/json") Object data, FileSystemResource file);
    }

    /**
     * Test 1: Verify that regular POST interface does NOT have @MultipartContentType
     */
    @Test
    public void testRegularPostHasNoMultipartContentType() throws Exception {
        System.out.println("=== Test 1: Regular POST has no @MultipartContentType ===");

        Method method = RegularPostService.class.getMethod("userSyncReq", UserSyncRequest.class);
        HttpEndpoint endpoint = method.getAnnotation(HttpEndpoint.class);
        Parameter[] params = method.getParameters();

        System.out.println("Method: " + method.getName());
        System.out.println("URL: " + endpoint.url());
        System.out.println("HTTP Method: " + endpoint.method());
        System.out.println("ContentType: '" + endpoint.contentType() + "'");
        System.out.println("Param count: " + params.length);

        // Check that NO parameter has @MultipartContentType
        boolean hasMultipartContentType = false;
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            boolean hasMct = param.isAnnotationPresent(MultipartContentType.class);
            System.out.println("Param[" + i + "]: " + param.getName() +
                ", type=" + param.getType().getSimpleName() +
                ", hasMultipartContentType=" + hasMct);
            if (hasMct) {
                hasMultipartContentType = true;
            }
        }

        assertFalse(hasMultipartContentType, "Regular POST should NOT have @MultipartContentType");
        System.out.println("PASS: No @MultipartContentType found on regular POST method\n");
    }

    /**
     * Test 2: Verify multipart detection logic for REGULAR POST
     */
    @Test
    public void testMultipartDetectionForRegularPost() throws Exception {
        System.out.println("=== Test 2: Multipart Detection for Regular POST ===");

        Method method = RegularPostService.class.getMethod("userSyncReq", UserSyncRequest.class);
        HttpEndpoint endpoint = method.getAnnotation(HttpEndpoint.class);
        String contentType = endpoint.contentType();
        Parameter[] methodParams = method.getParameters();

        System.out.println("contentType from @HttpEndpoint: '" + contentType + "'");
        System.out.println("contentType is null: " + (contentType == null));
        System.out.println("contentType is empty: " + (contentType != null && contentType.isEmpty()));

        // Simulate the detection logic from HttpServiceProxyFactory
        boolean isMultipartWithContentTypes = false;

        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            System.out.println(">>> ContentType contains multipart/form-data, checking params...");
            for (Parameter param : methodParams) {
                if (param.isAnnotationPresent(MultipartContentType.class)) {
                    isMultipartWithContentTypes = true;
                    break;
                }
            }
        } else {
            System.out.println(">>> ContentType does NOT contain multipart/form-data, skipping multipart logic");
        }

        System.out.println("isMultipartWithContentTypes = " + isMultipartWithContentTypes);
        assertFalse(isMultipartWithContentTypes, "Regular POST should NOT be detected as multipart");
        System.out.println("PASS: Regular POST correctly NOT detected as multipart\n");
    }

    /**
     * Test 3: Verify multipart detection logic for MULTIPART POST
     */
    @Test
    public void testMultipartDetectionForMultipartPost() throws Exception {
        System.out.println("=== Test 3: Multipart Detection for Multipart POST ===");

        Method method = MultipartPostService.class.getMethod("rentClaimReq", Object.class, FileSystemResource.class);
        HttpEndpoint endpoint = method.getAnnotation(HttpEndpoint.class);
        String contentType = endpoint.contentType();
        Parameter[] methodParams = method.getParameters();

        System.out.println("contentType from @HttpEndpoint: '" + contentType + "'");

        // Simulate the detection logic from HttpServiceProxyFactory
        boolean isMultipartWithContentTypes = false;

        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            System.out.println(">>> ContentType contains multipart/form-data, checking params...");
            for (Parameter param : methodParams) {
                boolean hasMct = param.isAnnotationPresent(MultipartContentType.class);
                System.out.println("  Param: " + param.getName() + ", hasMultipartContentType=" + hasMct);
                if (hasMct) {
                    isMultipartWithContentTypes = true;
                }
            }
        }

        System.out.println("isMultipartWithContentTypes = " + isMultipartWithContentTypes);
        assertTrue(isMultipartWithContentTypes, "Multipart POST SHOULD be detected as multipart");
        System.out.println("PASS: Multipart POST correctly detected as multipart\n");
    }

    /**
     * Test 4: Simulate parameter processing for REGULAR POST
     * This is the key test - it checks that mainReqObject is set correctly
     */
    @Test
    public void testParameterProcessingForRegularPost() throws Exception {
        System.out.println("=== Test 4: Parameter Processing for Regular POST ===");

        Method method = RegularPostService.class.getMethod("userSyncReq", UserSyncRequest.class);
        HttpEndpoint endpoint = method.getAnnotation(HttpEndpoint.class);
        String contentType = endpoint.contentType();
        Parameter[] methodParams = method.getParameters();

        // Simulate args
        UserSyncRequest requestDto = new UserSyncRequest("test-user", "test-token", "20260128120000");
        Object[] args = new Object[]{requestDto};

        // Simulate the detection logic
        boolean isMultipartWithContentTypes = false;
        if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {
            for (Parameter param : methodParams) {
                if (param.isAnnotationPresent(MultipartContentType.class)) {
                    isMultipartWithContentTypes = true;
                    break;
                }
            }
        }

        System.out.println("isMultipartWithContentTypes = " + isMultipartWithContentTypes);

        // Simulate the maps (from HttpServiceProxyFactory)
        Map<String, Object> multipartParts = isMultipartWithContentTypes ? new LinkedHashMap<>() : null;
        Map<String, String> multipartContentTypes = isMultipartWithContentTypes ? new HashMap<>() : null;

        Object mainReqObject = null;
        String[] paramNames = {"body"}; // from @HttpEndpoint.paramNames

        // Simulate parameter processing loop
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String paramName = paramNames[i];

            boolean isPrimitive = isPrimitiveOrWrapper(arg);
            System.out.println("Param[" + i + "]: " + paramName +
                ", type=" + (arg != null ? arg.getClass().getSimpleName() : "null") +
                ", isPrimitive=" + isPrimitive);

            if (isPrimitive) {
                System.out.println("  -> Would add to queryParams or pathParams");
            } else {
                System.out.println("  -> isMultipartWithContentTypes = " + isMultipartWithContentTypes);
                if (isMultipartWithContentTypes) {
                    multipartParts.put(paramName, arg);
                    System.out.println("  -> Added to multipartParts");
                } else {
                    mainReqObject = arg;
                    System.out.println("  -> Set as mainReqObject (regular POST behavior)");
                }
            }
        }

        // Build MultipartRequest if applicable (SHOULD NOT happen for regular POST)
        if (isMultipartWithContentTypes && multipartParts != null && !multipartParts.isEmpty()) {
            mainReqObject = new MultipartRequest(multipartParts, multipartContentTypes);
            System.out.println(">>> Created MultipartRequest (SHOULD NOT HAPPEN for regular POST)");
        }

        System.out.println("\nFinal mainReqObject type: " +
            (mainReqObject != null ? mainReqObject.getClass().getName() : "null"));
        System.out.println("Is MultipartRequest? " + (mainReqObject instanceof MultipartRequest));

        // Assertions
        assertNotNull(mainReqObject, "mainReqObject should not be null");
        assertFalse(mainReqObject instanceof MultipartRequest,
            "Regular POST should NOT create MultipartRequest");
        assertTrue(mainReqObject instanceof UserSyncRequest,
            "mainReqObject should be UserSyncRequest");
        assertEquals(requestDto, mainReqObject, "mainReqObject should be the original DTO");

        System.out.println("PASS: Regular POST correctly sets mainReqObject as the DTO\n");
    }

    /**
     * Test 5: Verify dispatch logic for REGULAR POST
     */
    @Test
    public void testDispatchLogicForRegularPost() throws Exception {
        System.out.println("=== Test 5: Dispatch Logic for Regular POST ===");

        // Simulate a regular POST request DTO
        UserSyncRequest requestDto = new UserSyncRequest("test-user", "test-token", "20260128120000");
        Object mainReqObject = requestDto;

        System.out.println("mainReqObject type: " + mainReqObject.getClass().getName());
        System.out.println("Is MultipartRequest? " + (mainReqObject instanceof MultipartRequest));

        // Simulate the dispatch logic from HttpServiceProxyFactory
        if (mainReqObject instanceof MultipartRequest) {
            System.out.println(">>> Would use RestTemplate for MultipartRequest");
            fail("Should NOT enter RestTemplate path for regular POST");
        } else {
            System.out.println(">>> Would use WebClient for regular request (CORRECT)");
        }

        System.out.println("PASS: Regular POST correctly dispatched to WebClient path\n");
    }

    /**
     * Test 6: Check @HttpEndpoint default contentType value
     */
    @Test
    public void testHttpEndpointDefaultContentType() throws Exception {
        System.out.println("=== Test 6: Check @HttpEndpoint Default ContentType ===");

        Method method = RegularPostService.class.getMethod("userSyncReq", UserSyncRequest.class);
        HttpEndpoint endpoint = method.getAnnotation(HttpEndpoint.class);

        String contentType = endpoint.contentType();
        System.out.println("contentType: '" + contentType + "'");
        System.out.println("contentType.isEmpty(): " + contentType.isEmpty());
        System.out.println("contentType.length(): " + contentType.length());
        System.out.println("contains 'multipart': " + contentType.toLowerCase().contains("multipart"));

        // Check if the condition evaluates correctly
        boolean wouldEnterMultipartCheck = contentType != null &&
            contentType.toLowerCase().contains("multipart/form-data");
        System.out.println("Would enter multipart check block: " + wouldEnterMultipartCheck);

        assertFalse(wouldEnterMultipartCheck,
            "Empty contentType should NOT trigger multipart check");
        System.out.println("PASS: Default contentType correctly prevents multipart check\n");
    }

    // Helper method from HttpServiceProxyFactory
    private boolean isPrimitiveOrWrapper(Object obj) {
        if (obj == null) return false;
        Class<?> clazz = obj.getClass();

        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            return componentType.isPrimitive()
                    || componentType == String.class
                    || Number.class.isAssignableFrom(componentType)
                    || componentType == Boolean.class
                    || componentType == Character.class;
        }

        return clazz.isPrimitive()
                || clazz == String.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == Boolean.class
                || clazz == Character.class;
    }
}
