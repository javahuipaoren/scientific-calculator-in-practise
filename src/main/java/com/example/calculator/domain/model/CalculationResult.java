package com.example.calculator.domain.model;

import com.example.calculator.domain.exception.InvalidExpressionException;

/**
 * 计算结果值对象，仅允许保存有限的双精度数值。
 *
 * @param value 计算结果
 */
public record CalculationResult(double value) {

    public CalculationResult {
        if (!Double.isFinite(value)) {
            throw new InvalidExpressionException("result is not finite");
        }
    }

    /**
     * 创建计算结果值对象。
     *
     * @param value 原始结果
     * @return 有限数值结果
     */
    public static CalculationResult of(double value) {
        return new CalculationResult(value);
    }
}
