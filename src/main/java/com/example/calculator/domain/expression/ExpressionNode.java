package com.example.calculator.domain.expression;

/**
 * 表达式抽象语法树节点。
 *
 * <p>解析器只负责构建表达式树，具体计算行为由可插拔的计算规则完成。
 */
public sealed interface ExpressionNode
        permits BinaryOperationNode,
                FunctionCallNode,
                IdentifierNode,
                NumberNode,
                UnaryOperationNode {
}
