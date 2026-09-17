package com.example.calculator.domain.rule.constant;

import com.example.calculator.domain.rule.ConstantRule;

/** 自然常数策略。 */
public final class EulerConstantRule implements ConstantRule {

    @Override
    public String name() {
        return "e";
    }

    @Override
    public double value() {
        return Math.E;
    }
}
