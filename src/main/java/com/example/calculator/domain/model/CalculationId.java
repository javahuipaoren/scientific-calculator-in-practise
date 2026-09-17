package com.example.calculator.domain.model;

/**
 * 计算记录标识值对象。
 *
 * @param value 大于零的唯一标识
 */
public record CalculationId(long value) {

    public CalculationId {
        if (value <= 0) {
            throw new IllegalArgumentException("计算记录 ID 必须大于零");
        }
    }

    /**
     * 创建计算记录标识。
     *
     * @param value 原始标识值
     * @return 计算记录标识
     */
    public static CalculationId of(long value) {
        return new CalculationId(value);
    }
}
