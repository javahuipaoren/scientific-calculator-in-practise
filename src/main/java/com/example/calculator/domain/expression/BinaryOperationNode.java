package com.example.calculator.domain.expression;

import java.util.Objects;

/**
 * 二元运算表达式节点。
 *
 * @param operator 运算符
 * @param left 左操作数节点
 * @param right 右操作数节点
 */
public record BinaryOperationNode(
        String operator,
        ExpressionNode left,
        ExpressionNode right) implements ExpressionNode {

    public BinaryOperationNode {
        Objects.requireNonNull(operator, "二元运算符不能为空");
        Objects.requireNonNull(left, "左操作数不能为空");
        Objects.requireNonNull(right, "右操作数不能为空");
    }
}
