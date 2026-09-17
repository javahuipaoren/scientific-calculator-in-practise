package com.example.calculator.infrastructure.persistence;

import com.example.calculator.domain.model.Calculation;
import com.example.calculator.domain.repository.CalculationHistoryRepository;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

/**
 * 计算历史仓储的内存实现。
 *
 * <p>使用有界双端队列保存记录。当容量达到上限时，新增记录会淘汰
 * 最旧记录，防止长期运行导致 JVM 内存无限增长。所有集合操作通过
 * 同一把锁保证快照一致性。
 */
public final class InMemoryCalculationHistoryRepository
        implements CalculationHistoryRepository {

    private final int capacity;
    private final ArrayDeque<Calculation> history = new ArrayDeque<>();

    public InMemoryCalculationHistoryRepository(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("历史容量必须大于零");
        }
        this.capacity = capacity;
    }

    @Override
    public synchronized void save(Calculation calculation) {
        Objects.requireNonNull(calculation, "计算记录不能为空");
        if (history.size() == capacity) {
            history.removeFirst();
        }
        history.addLast(calculation);
    }

    @Override
    public synchronized List<Calculation> findAll() {
        return List.copyOf(history);
    }

    @Override
    public synchronized void clear() {
        history.clear();
    }
}
