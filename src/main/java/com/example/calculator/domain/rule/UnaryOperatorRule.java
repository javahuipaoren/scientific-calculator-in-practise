package com.example.calculator.domain.rule;

/**
 * 一元运算策略扩展点。
 */
public interface UnaryOperatorRule {

    /** 运算符文本。 */
    String symbol();

    /** 一元运算符优先级。 */
    int precedence();

    /** 执行一元运算。 */
    double apply(double operand);
}
