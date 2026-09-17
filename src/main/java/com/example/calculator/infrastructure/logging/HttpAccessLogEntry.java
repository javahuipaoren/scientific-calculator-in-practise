package com.example.calculator.infrastructure.logging;

import java.time.Instant;
import java.util.Objects;

/**
 * 单次外部 HTTP 请求的访问日志记录。
 *
 * <p>记录只包含排障所需的协议元数据，不保存请求体、响应体、
 * 鉴权信息或表达式，避免在日志中泄露业务数据。
 *
 * @param occurredAt 请求完成时间
 * @param requestId 请求链路标识
 * @param method HTTP 方法
 * @param path 请求路径，不包含查询参数
 * @param status HTTP 响应状态码
 * @param durationMillis 请求处理耗时，单位毫秒
 * @param clientAddress 客户端网络地址
 */
public record HttpAccessLogEntry(
        Instant occurredAt,
        String requestId,
        String method,
        String path,
        int status,
        long durationMillis,
        String clientAddress) {

    public HttpAccessLogEntry {
        Objects.requireNonNull(occurredAt, "日志时间不能为空");
        Objects.requireNonNull(requestId, "请求标识不能为空");
        Objects.requireNonNull(method, "HTTP 方法不能为空");
        Objects.requireNonNull(path, "请求路径不能为空");
        Objects.requireNonNull(clientAddress, "客户端地址不能为空");
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("HTTP 状态码不合法: " + status);
        }
        if (durationMillis < 0) {
            throw new IllegalArgumentException("请求耗时不能为负数");
        }
    }
}
