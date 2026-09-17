package com.example.calculator.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 全局 HTTP 访问日志过滤器。
 *
 * <p>每次外部调用完成后输出包含请求标识、方法、路径、状态码、
 * 耗时和客户端地址的结构化单行日志，同时写入有界内存日志存储。
 * 请求体和查询参数不会写入日志。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpAccessLogFilter extends OncePerRequestFilter {

    /** 客户端可传递或服务端返回的链路标识请求头。 */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HttpAccessLogFilter.class);
    private static final int MAX_REQUEST_ID_LENGTH = 64;
    private static final Pattern VALID_REQUEST_ID = Pattern.compile(
            "[A-Za-z0-9._-]{1," + MAX_REQUEST_ID_LENGTH + "}");

    private final HttpAccessLogStore accessLogStore;
    private final Clock clock;

    public HttpAccessLogFilter(
            HttpAccessLogStore accessLogStore,
            Clock clock) {
        this.accessLogStore = accessLogStore;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        long startedAtNanos = System.nanoTime();
        Throwable failure = null;

        MDC.put("requestId", requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            failure = exception;
            throw exception;
        } finally {
            try {
                recordCompletedRequest(
                        request,
                        response,
                        requestId,
                        startedAtNanos,
                        failure);
            } finally {
                MDC.remove("requestId");
            }
        }
    }

    private void recordCompletedRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestId,
            long startedAtNanos,
            Throwable failure) {
        long elapsedNanos = System.nanoTime() - startedAtNanos;
        long durationMillis = TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        int status = resolveStatus(response, failure);
        HttpAccessLogEntry entry = new HttpAccessLogEntry(
                clock.instant(),
                requestId,
                request.getMethod(),
                request.getRequestURI(),
                status,
                durationMillis,
                request.getRemoteAddr());
        accessLogStore.append(entry);
        writeConsoleLog(entry);
    }

    private int resolveStatus(
            HttpServletResponse response,
            Throwable failure) {
        if (failure != null && response.getStatus() < 400) {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        return response.getStatus();
    }

    private void writeConsoleLog(HttpAccessLogEntry entry) {
        String template = "http_access method={} path={} status={} "
                + "durationMs={} clientIp={}";
        if (entry.status() >= 500) {
            LOGGER.error(
                    template,
                    entry.method(),
                    entry.path(),
                    entry.status(),
                    entry.durationMillis(),
                    entry.clientAddress());
        } else if (entry.status() >= 400) {
            LOGGER.warn(
                    template,
                    entry.method(),
                    entry.path(),
                    entry.status(),
                    entry.durationMillis(),
                    entry.clientAddress());
        } else {
            LOGGER.info(
                    template,
                    entry.method(),
                    entry.path(),
                    entry.status(),
                    entry.durationMillis(),
                    entry.clientAddress());
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String suppliedRequestId = request.getHeader(REQUEST_ID_HEADER);
        if (suppliedRequestId != null
                && VALID_REQUEST_ID.matcher(suppliedRequestId).matches()) {
            return suppliedRequestId;
        }
        return UUID.randomUUID().toString();
    }
}
