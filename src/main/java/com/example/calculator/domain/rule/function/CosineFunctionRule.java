package com.example.calculator.domain.rule.function;

/** 余弦函数策略，输入角度采用度数制。 */
public final class CosineFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "cos";
    }

    @Override
    protected double calculate(double argument) {
        return Math.cos(Math.toRadians(argument));
    }
}
