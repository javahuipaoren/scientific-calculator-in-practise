package com.example.calculator.domain.rule;

/**
 * 数学常量策略扩展点。
 */
public interface ConstantRule {

    /** 常量名称。 */
    String name();

    /** 常量值。 */
    double value();
}
