package com.example.calculator.interfaces.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 计算请求体大小限制过滤器。
 *
 * <p>在 Jackson 反序列化之前读取并限制请求体，防止调用方通过超大 JSON
 * 消耗过多内存。对没有发送 Content-Length 的分块请求同样有效。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestSizeFilter extends OncePerRequestFilter {

    /** 单个计算请求允许的最大字节数。 */
    public static final int MAX_REQUEST_BYTES = 16 * 1024;

    private static final String CALCULATION_PATH = "/api/calculations";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!isCalculationRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getContentLengthLong() > MAX_REQUEST_BYTES) {
            rejectOversizedRequest(response);
            return;
        }

        byte[] requestBody =
                request.getInputStream().readNBytes(MAX_REQUEST_BYTES + 1);
        if (requestBody.length > MAX_REQUEST_BYTES) {
            rejectOversizedRequest(response);
            return;
        }

        filterChain.doFilter(
                new CachedBodyRequest(request, requestBody), response);
    }

    private boolean isCalculationRequest(HttpServletRequest request) {
        return "POST".equals(request.getMethod())
                && (request.getContextPath() + CALCULATION_PATH)
                        .equals(request.getRequestURI());
    }

    private void rejectOversizedRequest(HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"code\":\"PAYLOAD_TOO_LARGE\","
                        + "\"error\":\"request body exceeds 16384 bytes\"}");
    }

    /**
     * 将已读取的请求体缓存起来，使后续 MVC 组件仍可正常读取 JSON。
     */
    private static final class CachedBodyRequest
            extends HttpServletRequestWrapper {

        private final byte[] requestBody;

        private CachedBodyRequest(
                HttpServletRequest request, byte[] requestBody) {
            super(request);
            this.requestBody = requestBody.clone();
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(requestBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                    getInputStream(), StandardCharsets.UTF_8));
        }
    }

    /** 基于内存字节数组的同步 Servlet 输入流。 */
    private static final class CachedBodyServletInputStream
            extends ServletInputStream {

        private final ByteArrayInputStream inputStream;

        private CachedBodyServletInputStream(byte[] requestBody) {
            this.inputStream = new ByteArrayInputStream(requestBody);
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("仅支持同步 JSON 请求");
        }
    }
}
