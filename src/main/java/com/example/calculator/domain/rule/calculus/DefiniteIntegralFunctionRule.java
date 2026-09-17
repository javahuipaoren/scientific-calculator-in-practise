package com.example.calculator.domain.rule.calculus;

import com.example.calculator.domain.engine.EvaluationContext;
import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.rule.FunctionRule;
import java.util.List;

/**
 * 定积分函数策略。
 *
 * <p>语法为 {@code integral(expression, lower, upper)}，被积变量固定为
 * {@code x}。本实现使用 1000 个偶数区间的 Simpson 法进行数值近似，目的是
 * 验证新计算类别可通过策略扩展接入；它不是符号积分引擎。
 */
public final class DefiniteIntegralFunctionRule implements FunctionRule {

    private static final String VARIABLE_NAME = "x";
    private static final int INTERVAL_COUNT = 1000;

    @Override
    public String name() {
        return "integral";
    }

    @Override
    public double apply(
            List<ExpressionNode> arguments,
            EvaluationContext context) {
        if (arguments.size() != 3) {
            throw new InvalidExpressionException(
                    "integral expects exactly 3 arguments");
        }

        ExpressionNode integrand = arguments.get(0);
        double lowerBound = context.evaluate(arguments.get(1));
        double upperBound = context.evaluate(arguments.get(2));
        if (lowerBound == upperBound) {
            return 0.0d;
        }

        double step = (upperBound - lowerBound) / INTERVAL_COUNT;
        double weightedSum = evaluateAt(context, integrand, lowerBound)
                + evaluateAt(context, integrand, upperBound);

        for (int index = 1; index < INTERVAL_COUNT; index++) {
            double x = lowerBound + index * step;
            int weight = index % 2 == 0 ? 2 : 4;
            weightedSum += weight * evaluateAt(context, integrand, x);
        }
        return weightedSum * step / 3.0d;
    }

    private double evaluateAt(
            EvaluationContext context,
            ExpressionNode integrand,
            double x) {
        return context.withVariable(VARIABLE_NAME, x).evaluate(integrand);
    }
}
