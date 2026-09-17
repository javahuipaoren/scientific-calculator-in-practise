package com.example.calculator.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * 计算记录聚合根。
 *
 * <p>聚合根将记录标识、表达式、结果和计算时间作为一个完整的
 * 一致性边界。
 */
public final class Calculation {

    private final CalculationId id;
    private final MathematicalExpression expression;
    private final CalculationResult result;
    private final Instant calculatedAt;

    private Calculation(
            CalculationId id,
            MathematicalExpression expression,
            CalculationResult result,
            Instant calculatedAt) {
        this.id = Objects.requireNonNull(id, "计算记录 ID 不能为空");
        this.expression = Objects.requireNonNull(expression, "数学表达式不能为空");
        this.result = Objects.requireNonNull(result, "计算结果不能为空");
        this.calculatedAt = Objects.requireNonNull(calculatedAt, "计算时间不能为空");
    }

    /**
     * 创建一条已经完成的计算记录。
     *
     * @param id 记录标识
     * @param expression 数学表达式
     * @param result 计算结果
     * @param calculatedAt 计算完成时间
     * @return 计算记录聚合根
     */
    public static Calculation completed(
            CalculationId id,
            MathematicalExpression expression,
            CalculationResult result,
            Instant calculatedAt) {
        return new Calculation(id, expression, result, calculatedAt);
    }

    public CalculationId id() {
        return id;
    }

    public MathematicalExpression expression() {
        return expression;
    }

    public CalculationResult result() {
        return result;
    }

    public Instant calculatedAt() {
        return calculatedAt;
    }
}
