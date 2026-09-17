package com.example.calculator.domain.model;

import com.example.calculator.domain.exception.InvalidExpressionException;

/**
 * 数学表达式值对象，负责维护表达式最基础的不变条件。
 *
 * @param value 用户输入的原始表达式
 */
public record MathematicalExpression(String value) {

    /** 表达式允许的最大字符数。 */
    public static final int MAX_LENGTH = 512;

    public MathematicalExpression {
        if (value == null || value.isBlank()) {
            throw new InvalidExpressionException("expression must not be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidExpressionException(
                    "expression exceeds " + MAX_LENGTH + " characters");
        }
    }

    /**
     * 创建数学表达式值对象。
     *
     * @param value 原始表达式
     * @return 已校验的表达式
     */
    public static MathematicalExpression of(String value) {
        return new MathematicalExpression(value);
    }
}
