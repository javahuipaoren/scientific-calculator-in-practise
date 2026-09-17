package com.example.calculator.domain.rule.calculus;

import com.example.calculator.domain.engine.EvaluationContext;
import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.rule.FunctionRule;
import java.util.List;

/**
 * 一阶导数函数策略。
 *
 * <p>语法为 {@code derivative(expression, point)}，变量固定为 {@code x}。
 * 使用中心差分进行数值近似，不承担符号求导职责。
 */
public final class DerivativeFunctionRule implements FunctionRule {

    private static final String VARIABLE_NAME = "x";
    private static final double RELATIVE_STEP = 1.0e-5d;

    @Override
    public String name() {
        return "derivative";
    }

    @Override
    public double apply(
            List<ExpressionNode> arguments,
            EvaluationContext context) {
        if (arguments.size() != 2) {
            throw new InvalidExpressionException(
                    "derivative expects exactly 2 arguments");
        }

        ExpressionNode functionExpression = arguments.get(0);
        double point = context.evaluate(arguments.get(1));
        double step = RELATIVE_STEP * Math.max(1.0d, Math.abs(point));
        double upperValue = context.withVariable(
                        VARIABLE_NAME, point + step)
                .evaluate(functionExpression);
        double lowerValue = context.withVariable(
                        VARIABLE_NAME, point - step)
                .evaluate(functionExpression);
        return (upperValue - lowerValue) / (2.0d * step);
    }
}
