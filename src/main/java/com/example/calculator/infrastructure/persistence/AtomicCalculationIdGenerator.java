package com.example.calculator.infrastructure.persistence;

import com.example.calculator.domain.model.CalculationId;
import com.example.calculator.domain.service.CalculationIdGenerator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于原子计数器的进程内计算记录 ID 生成器。
 *
 * <p>清空历史不会重置序列，从而避免同一次进程生命周期内出现
 * 重复标识。
 */
public final class AtomicCalculationIdGenerator implements CalculationIdGenerator {

    private final AtomicLong sequence = new AtomicLong();

    @Override
    public CalculationId nextId() {
        return CalculationId.of(sequence.incrementAndGet());
    }
}
