package com.example.calculator.domain.rule.function;

/** 自然指数函数策略。 */
public final class ExponentialFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    @Override
    public String name() {
        return "exp";
    }

    @Override
    protected double calculate(double argument) {
        return Math.exp(argument);
    }
}
