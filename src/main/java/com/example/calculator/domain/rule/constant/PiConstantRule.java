package com.example.calculator.domain.rule.constant;

import com.example.calculator.domain.rule.ConstantRule;

/** 圆周率常量策略。 */
public final class PiConstantRule implements ConstantRule {

    @Override
    public String name() {
        return "pi";
    }

    @Override
    public double value() {
        return Math.PI;
    }
}
