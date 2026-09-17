package com.example.calculator.domain.exception;

/**
 * 数学表达式不符合语法或领域规则时抛出的异常。
 */
public class InvalidExpressionException extends RuntimeException {

    public InvalidExpressionException(String message) {
        super(message);
    }
}
