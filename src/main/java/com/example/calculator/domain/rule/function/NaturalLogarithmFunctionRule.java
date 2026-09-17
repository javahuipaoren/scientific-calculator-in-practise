package com.example.calculator.domain.rule.function;

import com.example.calculator.domain.exception.InvalidExpressionException;

/** 自然对数函数策略。 */
public final class NaturalLogarithmFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "ln";
    }

    @Override
    protected double calculate(double argument) {
        if (argument <= 0.0d) {
            throw new InvalidExpressionException(
                    "ln requires a positive argument");
        }
        return Math.log(argument);
    }
}
