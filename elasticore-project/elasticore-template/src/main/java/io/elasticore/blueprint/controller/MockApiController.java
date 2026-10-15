package io.elasticore.blueprint.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock REST controller that simulates external API endpoints
 * for testing the HTTP Port feature of ElastiCORE.
 */
@Slf4j
@RestController
@RequestMapping("/api/mock")
public class MockApiController {

    @GetMapping("/echo/{message}")
    public Map<String, Object> echo(@PathVariable String message) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", "OK");
        resp.put("statusCode", 200);
        resp.put("receivedAt", LocalDateTime.now().toString());
        resp.put("echoMessage", message);
        return resp;
    }

    @PostMapping("/data")
    public Map<String, Object> postData(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", "CREATED");
        resp.put("statusCode", 201);
        resp.put("receivedAt", LocalDateTime.now().toString());
        resp.put("echoMessage", body.getOrDefault("message", ""));
        return resp;
    }

    @PutMapping("/users/{userId}")
    public Map<String, Object> updateUser(@PathVariable long userId,
                                          @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("userId", userId);
        resp.put("name", body.getOrDefault("name", ""));
        resp.put("email", body.getOrDefault("email", ""));
        resp.put("status", "UPDATED");
        return resp;
    }

    @DeleteMapping("/users/{userId}")
    public Map<String, Object> deleteUser(@PathVariable long userId) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("result", "DELETED");
        resp.put("statusCode", 200);
        resp.put("receivedAt", LocalDateTime.now().toString());
        resp.put("echoMessage", "User " + userId + " deleted");
        return resp;
    }

    @PatchMapping("/users/{userId}/status")
    public Map<String, Object> patchUser(@PathVariable long userId,
                                         @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("userId", userId);
        resp.put("name", "patched");
        resp.put("email", "patched@test.com");
        resp.put("status", "PATCHED");
        return resp;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestPart(value = "metadata", required = false) String metadata,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("fileName", file != null ? file.getOriginalFilename() : "no-file");
        resp.put("fileSize", file != null ? file.getSize() : 0L);
        resp.put("contentType", file != null ? file.getContentType() : "none");
        resp.put("message", "Upload received");
        return resp;
    }
}
