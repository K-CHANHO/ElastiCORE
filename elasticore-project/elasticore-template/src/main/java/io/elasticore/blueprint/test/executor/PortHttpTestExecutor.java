package io.elasticore.blueprint.test.executor;

import io.elasticore.blueprint.test.AbstractTestExecutor;
import io.elasticore.blueprint.test.TestCategory;
import io.elasticore.blueprint.test.TestResult;

import io.elasticore.blueprint.domain.integration.dto.HttpTestRequest;
import io.elasticore.blueprint.domain.integration.dto.HttpTestResponse;
import io.elasticore.blueprint.domain.integration.dto.HttpUserRequest;
import io.elasticore.blueprint.domain.integration.dto.HttpUserResponse;
import io.elasticore.blueprint.domain.integration.port.MockApiPortService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Test executor for PORT_HTTP category.
 * Validates the generated HTTP Port interface (MockApiPortService).
 * Falls back to direct REST calls via RestTemplate if the port bean is not available.
 */
@Slf4j
@Component
public class PortHttpTestExecutor extends AbstractTestExecutor {

    @Autowired(required = false)
    private MockApiPortService portService;

    @Value("${server.port:8080}")
    private int serverPort;

    private RestTemplate restTemplate;

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    private String baseUrl() {
        return "http://localhost:" + serverPort + "/api/mock";
    }

    @Override
    public TestCategory getCategory() {
        return TestCategory.PORT_HTTP;
    }

    /**
     * Tests HTTP GET via the port service or direct REST call.
     * Endpoint: GET /api/mock/echo/{message}
     */
    public TestResult testPortHttpGet() {
        long startTime = System.currentTimeMillis();
        try {
            if (portService != null) {
                // Use the generated port service bean
                HttpTestResponse response = portService.getEcho("hello");
                if (response == null) {
                    return failure("testPortHttpGet",
                            "Port getEcho() returned null", startTime, null);
                }
                log.info("testPortHttpGet via port: result={}, echoMessage={}",
                        response.getResult(), response.getEchoMessage());
                return success("testPortHttpGet",
                        "Port GET echo returned: result=" + response.getResult()
                                + ", echoMessage=" + response.getEchoMessage(), startTime);
            }

            // Fallback: direct REST call
            String url = baseUrl() + "/echo/hello";
            ResponseEntity<HttpTestResponse> resp = getRestTemplate()
                    .getForEntity(url, HttpTestResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                HttpTestResponse body = resp.getBody();
                log.info("testPortHttpGet via REST: statusCode={}, result={}",
                        resp.getStatusCode(), body.getResult());
                return success("testPortHttpGet",
                        "REST GET /echo/hello returned HTTP " + resp.getStatusCode().value()
                                + ", result=" + body.getResult(), startTime);
            }
            return failure("testPortHttpGet",
                    "Unexpected HTTP status: " + resp.getStatusCode(), startTime, null);
        } catch (Exception e) {
            log.error("testPortHttpGet failed", e);
            String msg = "Exception during HTTP GET: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                msg = "Connection refused - server may not be fully started";
            }
            return failure("testPortHttpGet", msg, startTime, e);
        }
    }

    /**
     * Tests HTTP POST via the port service or direct REST call.
     * Endpoint: POST /api/mock/data
     */
    public TestResult testPortHttpPost() {
        long startTime = System.currentTimeMillis();
        try {
            HttpTestRequest request = new HttpTestRequest();
            request.setMessage("test-post-data");
            request.setCode(100);

            if (portService != null) {
                HttpTestResponse response = portService.postData(request);
                if (response == null) {
                    return failure("testPortHttpPost",
                            "Port postData() returned null", startTime, null);
                }
                log.info("testPortHttpPost via port: result={}, statusCode={}",
                        response.getResult(), response.getStatusCode());
                return success("testPortHttpPost",
                        "Port POST data returned: result=" + response.getResult(), startTime);
            }

            // Fallback: direct REST call
            String url = baseUrl() + "/data";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HttpTestRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<HttpTestResponse> resp = getRestTemplate()
                    .postForEntity(url, entity, HttpTestResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                HttpTestResponse body = resp.getBody();
                log.info("testPortHttpPost via REST: statusCode={}, result={}",
                        resp.getStatusCode(), body.getResult());
                return success("testPortHttpPost",
                        "REST POST /data returned HTTP " + resp.getStatusCode().value()
                                + ", result=" + body.getResult(), startTime);
            }
            return failure("testPortHttpPost",
                    "Unexpected HTTP status: " + resp.getStatusCode(), startTime, null);
        } catch (Exception e) {
            log.error("testPortHttpPost failed", e);
            String msg = "Exception during HTTP POST: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                msg = "Connection refused - server may not be fully started";
            }
            return failure("testPortHttpPost", msg, startTime, e);
        }
    }

    /**
     * Tests HTTP PUT via the port service or direct REST call.
     * Endpoint: PUT /api/mock/users/{userId}
     */
    public TestResult testPortHttpPut() {
        long startTime = System.currentTimeMillis();
        try {
            HttpUserRequest request = new HttpUserRequest();
            request.setName("TestUser");
            request.setEmail("test@example.com");

            if (portService != null) {
                HttpUserResponse response = portService.updateUser(1L, request);
                if (response == null) {
                    return failure("testPortHttpPut",
                            "Port updateUser() returned null", startTime, null);
                }
                log.info("testPortHttpPut via port: userId={}, name={}, status={}",
                        response.getUserId(), response.getName(), response.getStatus());
                return success("testPortHttpPut",
                        "Port PUT user returned: userId=" + response.getUserId()
                                + ", name=" + response.getName(), startTime);
            }

            // Fallback: direct REST call
            String url = baseUrl() + "/users/1";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HttpUserRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<HttpUserResponse> resp = getRestTemplate()
                    .exchange(url, HttpMethod.PUT, entity, HttpUserResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                HttpUserResponse body = resp.getBody();
                log.info("testPortHttpPut via REST: statusCode={}, userId={}",
                        resp.getStatusCode(), body.getUserId());
                return success("testPortHttpPut",
                        "REST PUT /users/1 returned HTTP " + resp.getStatusCode().value()
                                + ", userId=" + body.getUserId(), startTime);
            }
            return failure("testPortHttpPut",
                    "Unexpected HTTP status: " + resp.getStatusCode(), startTime, null);
        } catch (Exception e) {
            log.error("testPortHttpPut failed", e);
            String msg = "Exception during HTTP PUT: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                msg = "Connection refused - server may not be fully started";
            }
            return failure("testPortHttpPut", msg, startTime, e);
        }
    }

    /**
     * Tests HTTP DELETE via the port service or direct REST call.
     * Endpoint: DELETE /api/mock/users/{userId}
     */
    public TestResult testPortHttpDelete() {
        long startTime = System.currentTimeMillis();
        try {
            if (portService != null) {
                HttpTestResponse response = portService.deleteUser(1L);
                if (response == null) {
                    return failure("testPortHttpDelete",
                            "Port deleteUser() returned null", startTime, null);
                }
                log.info("testPortHttpDelete via port: result={}, statusCode={}",
                        response.getResult(), response.getStatusCode());
                return success("testPortHttpDelete",
                        "Port DELETE user returned: result=" + response.getResult(), startTime);
            }

            // Fallback: direct REST call
            String url = baseUrl() + "/users/1";
            ResponseEntity<HttpTestResponse> resp = getRestTemplate()
                    .exchange(url, HttpMethod.DELETE, null, HttpTestResponse.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                HttpTestResponse body = resp.getBody();
                String result = body != null ? body.getResult() : "no body";
                log.info("testPortHttpDelete via REST: statusCode={}, result={}",
                        resp.getStatusCode(), result);
                return success("testPortHttpDelete",
                        "REST DELETE /users/1 returned HTTP " + resp.getStatusCode().value()
                                + ", result=" + result, startTime);
            }
            return failure("testPortHttpDelete",
                    "Unexpected HTTP status: " + resp.getStatusCode(), startTime, null);
        } catch (Exception e) {
            log.error("testPortHttpDelete failed", e);
            String msg = "Exception during HTTP DELETE: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                msg = "Connection refused - server may not be fully started";
            }
            return failure("testPortHttpDelete", msg, startTime, e);
        }
    }

    /**
     * Tests HTTP PATCH via the port service or direct REST call.
     * Endpoint: PATCH /api/mock/users/{userId}/status
     */
    public TestResult testPortHttpPatch() {
        long startTime = System.currentTimeMillis();
        try {
            HttpTestRequest request = new HttpTestRequest();
            request.setMessage("status-update");
            request.setCode(1);

            if (portService != null) {
                HttpUserResponse response = portService.patchUser(1L, request);
                if (response == null) {
                    return failure("testPortHttpPatch",
                            "Port patchUser() returned null", startTime, null);
                }
                log.info("testPortHttpPatch via port: userId={}, status={}",
                        response.getUserId(), response.getStatus());
                return success("testPortHttpPatch",
                        "Port PATCH user returned: userId=" + response.getUserId()
                                + ", status=" + response.getStatus(), startTime);
            }

            // Fallback: direct REST call
            String url = baseUrl() + "/users/1/status";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HttpTestRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<HttpUserResponse> resp = getRestTemplate()
                    .exchange(url, HttpMethod.PATCH, entity, HttpUserResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                HttpUserResponse body = resp.getBody();
                log.info("testPortHttpPatch via REST: statusCode={}, userId={}",
                        resp.getStatusCode(), body.getUserId());
                return success("testPortHttpPatch",
                        "REST PATCH /users/1/status returned HTTP " + resp.getStatusCode().value()
                                + ", status=" + body.getStatus(), startTime);
            }
            return failure("testPortHttpPatch",
                    "Unexpected HTTP status: " + resp.getStatusCode(), startTime, null);
        } catch (Exception e) {
            log.error("testPortHttpPatch failed", e);
            String msg = "Exception during HTTP PATCH: " + e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                msg = "Connection refused - server may not be fully started";
            }
            return failure("testPortHttpPatch", msg, startTime, e);
        }
    }
}
