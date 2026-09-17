package com.example.calculator.domain.rule.operator;

import com.example.calculator.domain.rule.Associativity;
import com.example.calculator.domain.rule.BinaryOperatorRule;

/** 减法运算策略。 */
public final class SubtractOperatorRule implements BinaryOperatorRule {

    @Override
    public String symbol() {
        return "-";
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Associativity associativity() {
        return Associativity.LEFT;
    }

    @Override
    public double apply(double left, double right) {
        return left - right;
    }
}
