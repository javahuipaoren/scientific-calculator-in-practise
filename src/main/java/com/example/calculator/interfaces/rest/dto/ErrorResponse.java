package com.example.calculator.interfaces.rest.dto;

/**
 * 统一 HTTP 错误响应。
 *
 * @param code 稳定的机器可读错误码
 * @param error 面向调用方的错误描述
 */
public record ErrorResponse(String code, String error) {
}
