package com.example.calculator.interfaces.rest.dto;

import com.example.calculator.application.result.CalculationView;
import java.time.Instant;

/**
 * 科学计算 HTTP 响应。
 *
 * @param id 记录标识
 * @param expression 原始表达式
 * @param result 计算结果
 * @param createdAt 计算完成时间
 */
public record CalculationResponse(
        long id,
        String expression,
        double result,
        Instant createdAt) {

    /** 将应用层视图转换为 HTTP 响应。 */
    public static CalculationResponse from(CalculationView view) {
        return new CalculationResponse(
                view.id(),
                view.expression(),
                view.result(),
                view.calculatedAt());
    }
}
