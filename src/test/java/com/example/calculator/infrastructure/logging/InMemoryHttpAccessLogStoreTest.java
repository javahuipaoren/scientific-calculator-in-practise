package com.example.calculator.infrastructure.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 有界 HTTP 访问日志内存存储测试。 */
@DisplayName("HTTP 访问日志内存存储")
class InMemoryHttpAccessLogStoreTest {

    @Test
    @DisplayName("容量达到上限后应淘汰最旧日志")
    void shouldEvictOldestEntryWhenCapacityReached() {
        InMemoryHttpAccessLogStore store =
                new InMemoryHttpAccessLogStore(2);

        store.append(entry("request-1", "/first"));
        store.append(entry("request-2", "/second"));
        store.append(entry("request-3", "/third"));

        List<HttpAccessLogEntry> snapshot = store.snapshot();
        assertEquals(2, snapshot.size());
        assertEquals("request-2", snapshot.get(0).requestId());
        assertEquals("request-3", snapshot.get(1).requestId());
    }

    @Test
    @DisplayName("日志快照应只读且支持清空")
    void shouldReturnImmutableSnapshotAndClearEntries() {
        InMemoryHttpAccessLogStore store =
                new InMemoryHttpAccessLogStore(2);
        store.append(entry("request-1", "/first"));

        List<HttpAccessLogEntry> snapshot = store.snapshot();
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(entry("request-2", "/second")));

        store.clear();
        assertEquals(0, store.snapshot().size());
        assertEquals(1, snapshot.size());
    }

    private HttpAccessLogEntry entry(String requestId, String path) {
        return new HttpAccessLogEntry(
                Instant.parse("2026-09-15T00:00:00Z"),
                requestId,
                "GET",
                path,
                200,
                1L,
                "127.0.0.1");
    }
}
