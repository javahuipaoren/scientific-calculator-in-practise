package com.example.calculator.domain.rule.operator;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.rule.Associativity;
import com.example.calculator.domain.rule.BinaryOperatorRule;

/** 除法运算策略，负责维护除数不能为零的领域规则。 */
public final class DivideOperatorRule implements BinaryOperatorRule {

    @Override
    public String symbol() {
        return "/";
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
            throw new InvalidExpressionException("division by zero");
        }
        return left / right;
    }
}
