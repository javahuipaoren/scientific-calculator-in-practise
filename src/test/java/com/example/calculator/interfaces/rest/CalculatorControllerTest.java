package com.example.calculator.interfaces.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.calculator.infrastructure.logging.HttpAccessLogEntry;
import com.example.calculator.infrastructure.logging.HttpAccessLogFilter;
import com.example.calculator.infrastructure.logging.HttpAccessLogStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 科学计算 REST 接口集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("科学计算 REST 接口")
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HttpAccessLogStore accessLogStore;

    @BeforeEach
    void clearHistory() throws Exception {
        mockMvc.perform(delete("/api/calculations"))
                .andExpect(status().isNoContent());
        accessLogStore.clear();
    }

    @Test
    @DisplayName("每次外部调用都应返回链路标识并记录访问日志")
    void shouldReturnRequestIdAndRecordAccessLog() throws Exception {
        String requestId = "sre-test-request-001";

        mockMvc.perform(get("/api/calculations")
                        .header(HttpAccessLogFilter.REQUEST_ID_HEADER, requestId))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpAccessLogFilter.REQUEST_ID_HEADER, requestId));

        List<HttpAccessLogEntry> entries = accessLogStore.snapshot();
        assertFalse(entries.isEmpty());
        HttpAccessLogEntry latest = entries.get(entries.size() - 1);
        assertEquals(requestId, latest.requestId());
        assertEquals("GET", latest.method());
        assertEquals("/api/calculations", latest.path());
        assertEquals(200, latest.status());
    }

    @Test
    @DisplayName("应完成计算、查询历史和清空历史")
    void shouldCalculateFindAndClearHistory() throws Exception {
        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"sqrt(9)+sin(30)*2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.expression")
                        .value("sqrt(9)+sin(30)*2"))
                .andExpect(jsonPath("$.result").value(4.0d))
                .andExpect(jsonPath("$.createdAt").exists());

        mockMvc.perform(get("/api/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/calculations"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/calculations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("应通过 HTTP 接口执行可插拔微积分策略")
    void shouldCalculateCalculusFunctions() throws Exception {
        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"integral(x^2,0,1)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result")
                        .value(org.hamcrest.Matchers.closeTo(
                                1.0d / 3.0d, 1.0e-9d), Double.class));

        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"derivative(x^3,2)\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result")
                        .value(org.hamcrest.Matchers.closeTo(
                                12.0d, 1.0e-6d), Double.class));
    }

    @Test
    @DisplayName("非法表达式应返回统一领域错误")
    void shouldReturnBadRequestForInvalidExpression() throws Exception {
        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"1/0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_EXPRESSION"))
                .andExpect(jsonPath("$.error").value("division by zero"));
    }

    @Test
    @DisplayName("空表达式和格式错误 JSON 应返回 HTTP 400")
    void shouldReturnBadRequestForInvalidJsonRequest() throws Exception {
        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("HTTP_400"));

        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("HTTP_400"));
    }

    @Test
    @DisplayName("超大请求体应在反序列化前被拒绝")
    void shouldRejectOversizedRequestBody() throws Exception {
        String oversizedBody = "{\"expression\":\""
                + "1".repeat(17_000)
                + "\"}";

        mockMvc.perform(post("/api/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(oversizedBody))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code")
                        .value("PAYLOAD_TOO_LARGE"));
    }
}
