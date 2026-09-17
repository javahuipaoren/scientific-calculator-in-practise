package com.example.calculator.domain.rule.function;

import com.example.calculator.domain.exception.InvalidExpressionException;

/** 平方根函数策略。 */
public final class SquareRootFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "sqrt";
    }

    @Override
    protected double calculate(double argument) {
        if (argument < 0.0d) {
            throw new InvalidExpressionException(
                    "sqrt requires a non-negative argument");
        }
        return Math.sqrt(argument);
    }
}
