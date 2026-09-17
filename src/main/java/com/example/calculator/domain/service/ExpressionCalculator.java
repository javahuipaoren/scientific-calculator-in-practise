package com.example.calculator.domain.service;

import com.example.calculator.domain.model.CalculationResult;
import com.example.calculator.domain.model.MathematicalExpression;

/**
 * 表达式计算领域服务。
 *
 * <p>当运算行为不自然归属于某个值对象时，由领域服务承载该行为。
 */
public interface ExpressionCalculator {

    /**
     * 计算数学表达式。
     *
     * @param expression 已通过基础校验的表达式
     * @return 计算结果
     */
    CalculationResult calculate(MathematicalExpression expression);
}
