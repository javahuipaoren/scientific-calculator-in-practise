package com.example.calculator.domain.engine;

import com.example.calculator.domain.expression.ExpressionNode;

/**
 * 表达式求值上下文。
 *
 * <p>高级函数可以创建绑定变量的新上下文，并对同一表达式树重复求值。
 */
public interface EvaluationContext {

    /** 对表达式树求值。 */
    double evaluate(ExpressionNode expression);

    /**
     * 创建绑定指定变量的新上下文，原上下文保持不变。
     *
     * @param variableName 变量名
     * @param value 变量值
     * @return 新的不可变求值上下文
     */
    EvaluationContext withVariable(String variableName, double value);
}
