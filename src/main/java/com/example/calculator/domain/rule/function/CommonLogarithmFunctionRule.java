package com.example.calculator.domain.rule.function;

import com.example.calculator.domain.exception.InvalidExpressionException;

/** 常用对数函数策略。 */
public final class CommonLogarithmFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "log";
    }

    @Override
    protected double calculate(double argument) {
        if (argument <= 0.0d) {
            throw new InvalidExpressionException(
                    "log requires a positive argument");
        }
        return Math.log10(argument);
    }
}
