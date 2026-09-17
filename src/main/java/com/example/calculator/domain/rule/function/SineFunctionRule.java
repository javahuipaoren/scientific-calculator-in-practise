package com.example.calculator.domain.rule.function;

/** 正弦函数策略，输入角度采用度数制。 */
public final class SineFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "sin";
    }

    @Override
    protected double calculate(double argument) {
        return Math.sin(Math.toRadians(argument));
    }
}
