package com.example.calculator.domain.rule.operator;

import com.example.calculator.domain.rule.UnaryOperatorRule;

/** 一元正号运算策略。 */
public final class UnaryPlusOperatorRule implements UnaryOperatorRule {

    @Override
    public String symbol() {
        return "+";
    }

    @Override
    public int precedence() {
        return 25;
    }

    @Override
    public double apply(double operand) {
        return operand;
    }
}
