package com.example.calculator.domain.expression;

import java.util.Objects;

/**
 * 一元运算表达式节点。
 *
 * @param operator 运算符
 * @param operand 操作数节点
 */
public record UnaryOperationNode(
        String operator,
        ExpressionNode operand) implements ExpressionNode {

    public UnaryOperationNode {
        Objects.requireNonNull(operator, "一元运算符不能为空");
        Objects.requireNonNull(operand, "一元操作数不能为空");
    }
}
