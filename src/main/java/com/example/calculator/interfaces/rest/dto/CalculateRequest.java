package com.example.calculator.interfaces.rest.dto;

import com.example.calculator.domain.model.MathematicalExpression;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 科学计算 HTTP 请求。
 *
 * @param expression 待计算的数学表达式
 */
public record CalculateRequest(
        @NotBlank(message = "expression must not be blank")
        @Size(
                max = MathematicalExpression.MAX_LENGTH,
                message = "expression exceeds 512 characters")
        String expression) {
}
