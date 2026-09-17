package com.example.calculator.application.command;

/**
 * 执行科学计算的应用命令。
 *
 * @param expression 用户提交的数学表达式
 */
public record CalculateExpressionCommand(String expression) {
}
