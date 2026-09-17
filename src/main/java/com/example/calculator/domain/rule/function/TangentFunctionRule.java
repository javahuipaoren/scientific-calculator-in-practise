package com.example.calculator.domain.rule.function;

import com.example.calculator.domain.exception.InvalidExpressionException;

/** 正切函数策略，输入角度采用度数制。 */
public final class TangentFunctionRule
        extends AbstractSingleArgumentFunctionRule {

    private static final double UNDEFINED_TOLERANCE = 1.0e-12d;

    @Override
    public String name() {
        return "tan";
    }

    @Override
    protected double calculate(double argument) {
        double radians = Math.toRadians(argument);
        if (Math.abs(Math.cos(radians)) < UNDEFINED_TOLERANCE) {
            throw new InvalidExpressionException(
                    "tan is undefined for this argument");
        }
        return Math.tan(radians);
    }
}
