package com.example.calculator.domain.rule.operator;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.rule.Associativity;
import com.example.calculator.domain.rule.BinaryOperatorRule;

/** 取模运算策略，负责维护模数不能为零的领域规则。 */
public final class ModuloOperatorRule implements BinaryOperatorRule {

    @Override
    public String symbol() {
        return "%";
    }

    @Override
    public int precedence() {
        return 20;
    }

    @Override
    public Associativity associativity() {
        return Associativity.LEFT;
    }

    @Override
    public double apply(double left, double right) {
        if (right == 0.0d) {
            throw new InvalidExpressionException("modulo by zero");
        }
        return left % right;
    }
}
