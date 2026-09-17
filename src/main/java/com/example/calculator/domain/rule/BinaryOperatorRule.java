package com.example.calculator.domain.rule;

/**
 * 二元运算策略扩展点。
 *
 * <p>新增二元运算符时实现该接口并注册到规则注册表，无需修改解析
 * 模板或求值模板。
 */
public interface BinaryOperatorRule {

    /** 运算符文本。 */
    String symbol();

    /** 优先级，数值越大优先级越高。 */
    int precedence();

    /** 运算符结合方向。 */
    Associativity associativity();

    /** 执行二元运算。 */
    double apply(double left, double right);
}
