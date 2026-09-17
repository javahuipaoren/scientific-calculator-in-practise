package com.example.calculator.domain.rule.operator;

import com.example.calculator.domain.rule.Associativity;
import com.example.calculator.domain.rule.BinaryOperatorRule;

/** 幂运算策略，采用右结合语义。 */
public final class PowerOperatorRule implements BinaryOperatorRule {

    @Override
    public String symbol() {
        return "^";
    }

    @Override
    public int precedence() {
        return 30;
    }

    @Override
    public Associativity associativity() {
        return Associativity.RIGHT;
    }

    @Override
    public double apply(double left, double right) {
        return Math.pow(left, right);
    }
}
