package com.example.calculator.infrastructure.logging;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 有界的 HTTP 访问日志内存存储。
 *
 * <p>访问日志只会按时间追加，不存在按键访问场景，因此采用
 * “淘汰最旧记录”的滚动队列比传统 LRU 更合适。容量达到上限后
 * 删除最早日志，避免长期运行时内存无限增长。
 */
public final class InMemoryHttpAccessLogStore implements HttpAccessLogStore {

    private final int capacity;
    private final Deque<HttpAccessLogEntry> entries;
    private final ReentrantLock lock = new ReentrantLock();

    public InMemoryHttpAccessLogStore(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("访问日志容量必须大于零");
        }
        this.capacity = capacity;
        this.entries = new ArrayDeque<>(capacity);
    }

    @Override
    public void append(HttpAccessLogEntry entry) {
        Objects.requireNonNull(entry, "访问日志不能为空");
        lock.lock();
        try {
            if (entries.size() == capacity) {
                entries.removeFirst();
            }
            entries.addLast(entry);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<HttpAccessLogEntry> snapshot() {
        lock.lock();
        try {
            return List.copyOf(new ArrayList<>(entries));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            entries.clear();
        } finally {
            lock.unlock();
        }
    }
}
