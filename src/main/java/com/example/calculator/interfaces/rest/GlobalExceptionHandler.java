package com.example.calculator.interfaces.rest;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.interfaces.rest.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * REST 接口统一异常处理器。
 *
 * <p>领域异常转换为 HTTP 400；框架产生的 HTTP 异常保留原始状态码；
 * 未知异常隐藏内部细节并记录日志。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 将表达式领域异常转换为稳定的错误协议。 */
    @ExceptionHandler(InvalidExpressionException.class)
    ResponseEntity<Object> handleInvalidExpression(
            InvalidExpressionException exception) {
        ErrorResponse body = new ErrorResponse(
                "INVALID_EXPRESSION", exception.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 统一处理 JSON 格式错误、参数校验失败、错误方法和错误 Content-Type 等
     * 框架异常。
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = switch (status.value()) {
            case 400 ->
                    "invalid request: expected JSON with a nonblank expression "
                            + "of at most 512 characters";
            case 404 -> "resource not found";
            case 405 -> "method not allowed";
            case 415 -> "content type must be application/json";
            default -> "HTTP request failed";
        };
        ErrorResponse errorResponse =
                new ErrorResponse("HTTP_" + status.value(), message);
        return new ResponseEntity<>(errorResponse, headers, status);
    }

    /** 兜底处理未预期异常，避免向客户端泄露堆栈和实现细节。 */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> handleUnexpectedException(Exception exception) {
        LOGGER.error("处理计算请求时发生未预期异常", exception);
        ErrorResponse body =
                new ErrorResponse("INTERNAL_ERROR", "internal server error");
        return ResponseEntity.internalServerError().body(body);
    }
}
