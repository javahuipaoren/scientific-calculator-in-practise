package com.example.calculator.domain.rule.function;

/** 绝对值函数策略。 */
public final class AbsoluteFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "abs";
    }

    @Override
    protected double calculate(double argument) {
        return Math.abs(argument);
    }
}
