package com.example.calculator.domain.service;

import com.example.calculator.domain.model.CalculationId;

/**
 * 计算记录标识生成器端口。
 */
public interface CalculationIdGenerator {

    /**
     * 生成下一个进程内唯一标识。
     *
     * @return 新的计算记录标识
     */
    CalculationId nextId();
}
