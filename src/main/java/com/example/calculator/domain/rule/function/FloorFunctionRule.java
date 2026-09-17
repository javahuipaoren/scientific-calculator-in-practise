package com.example.calculator.domain.rule.function;

/** 向下取整函数策略。 */
public final class FloorFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "floor";
    }

    @Override
    protected double calculate(double argument) {
        return Math.floor(argument);
    }
}
