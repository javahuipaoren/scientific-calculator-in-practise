package com.example.calculator.domain.expression;

import java.util.List;
import java.util.Objects;

/**
 * 函数调用表达式节点。
 *
 * @param name 函数名称
 * @param arguments 未求值的参数表达式树
 */
public record FunctionCallNode(
        String name,
        List<ExpressionNode> arguments) implements ExpressionNode {

    public FunctionCallNode {
        Objects.requireNonNull(name, "函数名称不能为空");
        arguments = List.copyOf(arguments);
    }
}
