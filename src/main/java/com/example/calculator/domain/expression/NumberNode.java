package com.example.calculator.domain.expression;

/**
 * 数值字面量节点。
 *
 * @param value 数值
 */
public record NumberNode(double value) implements ExpressionNode {
}
