package com.example.calculator.domain.expression;

import java.util.Objects;

/**
 * 标识符节点，可表示数学常量或微积分表达式中的变量。
 *
 * @param name 标识符名称
 */
public record IdentifierNode(String name) implements ExpressionNode {

    public IdentifierNode {
        Objects.requireNonNull(name, "标识符名称不能为空");
    }
}
