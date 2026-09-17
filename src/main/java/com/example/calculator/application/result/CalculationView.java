package com.example.calculator.application.result;

import com.example.calculator.domain.model.Calculation;
import java.time.Instant;

/**
 * 提供给接口层的计算记录只读视图。
 *
 * @param id 记录标识
 * @param expression 原始表达式
 * @param result 计算结果
 * @param calculatedAt 计算时间
 */
public record CalculationView(
        long id,
        String expression,
        double result,
        Instant calculatedAt) {

    /**
     * 将领域聚合转换为应用层只读视图。
     *
     * @param calculation 计算聚合
     * @return 计算记录视图
     */
    public static CalculationView from(Calculation calculation) {
        return new CalculationView(
                calculation.id().value(),
                calculation.expression().value(),
                calculation.result().value(),
                calculation.calculatedAt());
    }
}
