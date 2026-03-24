package com.mahjong.service;

import com.mahjong.entity.ApiCallLog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ApiCallLogServiceTest {

    @Autowired
    private ApiCallLogService apiCallLogService;

    @Test
    void testLogPersistsRecord() {
        ApiCallLog saved = apiCallLogService.log(
            "/api/suggest-move", "POST", "{\"hand\":[]}", 200, 42L, null
        );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("/api/suggest-move", saved.getEndpoint());
        assertEquals("POST", saved.getHttpMethod());
        assertEquals(200, saved.getResponseStatus());
        assertEquals(42L, saved.getDurationMs());
        assertNull(saved.getErrorMessage());
    }

    @Test
    void testLogWithErrorMessagePersisted() {
        ApiCallLog saved = apiCallLogService.log(
            "/api/suggest-move", "POST", "{}", 400, 5L, "Invalid request"
        );

        assertNotNull(saved.getId());
        assertEquals(400, saved.getResponseStatus());
        assertEquals("Invalid request", saved.getErrorMessage());
    }

    @Test
    void testFindByIdReturnsRecord() {
        ApiCallLog saved = apiCallLogService.log("/api/health", "GET", null, 200, 1L, null);

        Optional<ApiCallLog> found = apiCallLogService.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testFindAllReturnsAllRecords() {
        apiCallLogService.log("/api/suggest-move", "POST", "{}", 200, 10L, null);
        apiCallLogService.log("/api/evaluate-call", "POST", "{}", 200, 15L, null);

        List<ApiCallLog> all = apiCallLogService.findAll();

        assertTrue(all.size() >= 2);
    }

    @Test
    void testFindByEndpointFiltersCorrectly() {
        apiCallLogService.log("/api/suggest-move", "POST", "{}", 200, 10L, null);
        apiCallLogService.log("/api/evaluate-call", "POST", "{}", 200, 15L, null);
        apiCallLogService.log("/api/suggest-move", "POST", "{}", 400, 3L, "error");

        List<ApiCallLog> results = apiCallLogService.findByEndpoint("/api/suggest-move");

        assertTrue(results.size() >= 2);
        results.forEach(log -> assertEquals("/api/suggest-move", log.getEndpoint()));
    }

    @Test
    void testNullRequestBodyAllowed() {
        ApiCallLog saved = apiCallLogService.log("/api/health", "GET", null, 200, 1L, null);

        assertNotNull(saved.getId());
        assertNull(saved.getRequestBody());
    }

    @Test
    void testFindAllOrderedMostRecentFirst() {
        ApiCallLog first = apiCallLogService.log("/api/suggest-move", "POST", "{}", 200, 10L, null);
        ApiCallLog second = apiCallLogService.log("/api/evaluate-call", "POST", "{}", 200, 5L, null);

        List<ApiCallLog> all = apiCallLogService.findAll();

        assertTrue(all.size() >= 2);
        assertTrue(all.get(0).getCreatedAt().isAfter(all.get(all.size() - 1).getCreatedAt())
            || all.get(0).getCreatedAt().isEqual(all.get(all.size() - 1).getCreatedAt()));
    }
}
