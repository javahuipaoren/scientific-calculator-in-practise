package com.example.calculator.domain.rule;

/**
 * 二元运算符结合方向。
 */
public enum Associativity {
    /** 左结合，例如加法和乘法。 */
    LEFT,

    /** 右结合，例如幂运算。 */
    RIGHT
}
