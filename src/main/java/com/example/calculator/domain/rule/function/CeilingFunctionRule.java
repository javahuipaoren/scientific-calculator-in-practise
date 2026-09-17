package com.example.calculator.domain.rule.function;

/** 向上取整函数策略。 */
public final class CeilingFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "ceil";
    }

    @Override
    protected double calculate(double argument) {
        return Math.ceil(argument);
    }
}
