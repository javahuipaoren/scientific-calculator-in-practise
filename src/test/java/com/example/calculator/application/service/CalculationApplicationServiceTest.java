package com.example.calculator.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.calculator.application.command.CalculateExpressionCommand;
import com.example.calculator.application.result.CalculationView;
import com.example.calculator.domain.repository.CalculationHistoryRepository;
import com.example.calculator.domain.service.CalculationIdGenerator;
import com.example.calculator.support.TestCalculatorFactory;
import com.example.calculator.infrastructure.persistence.AtomicCalculationIdGenerator;
import com.example.calculator.infrastructure.persistence.InMemoryCalculationHistoryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 计算应用服务测试，验证用例编排和仓储交互结果。
 */
@DisplayName("计算应用服务")
class CalculationApplicationServiceTest {

    private CalculationApplicationService applicationService;

    @BeforeEach
    void setUp() {
        CalculationHistoryRepository repository =
                new InMemoryCalculationHistoryRepository(2);
        CalculationIdGenerator idGenerator =
                new AtomicCalculationIdGenerator();
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-09-15T00:00:00Z"), ZoneOffset.UTC);

        applicationService = new CalculationApplicationService(
                TestCalculatorFactory.createCalculator(),
                repository,
                idGenerator,
                fixedClock);
    }

    @Test
    @DisplayName("计算后应生成聚合并保存历史")
    void shouldCalculateAndSaveHistory() {
        CalculationView view = applicationService.calculate(
                new CalculateExpressionCommand("2 + 3"));

        assertEquals(1L, view.id());
        assertEquals("2 + 3", view.expression());
        assertEquals(5.0d, view.result());
        assertEquals(Instant.parse("2026-09-15T00:00:00Z"),
                view.calculatedAt());
        assertEquals(1, applicationService.findHistory().size());
    }

    @Test
    @DisplayName("仓储达到容量后应淘汰最旧记录")
    void shouldEvictOldestHistoryWhenCapacityReached() {
        applicationService.calculate(new CalculateExpressionCommand("1 + 1"));
        applicationService.calculate(new CalculateExpressionCommand("2 + 2"));
        applicationService.calculate(new CalculateExpressionCommand("3 + 3"));

        assertEquals(2, applicationService.findHistory().size());
        assertEquals(2L, applicationService.findHistory().get(0).id());
        assertEquals(3L, applicationService.findHistory().get(1).id());
    }

    @Test
    @DisplayName("清空历史不应重置 ID 序列")
    void shouldClearHistoryWithoutResettingSequence() {
        applicationService.calculate(new CalculateExpressionCommand("1 + 1"));
        applicationService.clearHistory();

        assertTrue(applicationService.findHistory().isEmpty());
        CalculationView next = applicationService.calculate(
                new CalculateExpressionCommand("2 + 2"));
        assertEquals(2L, next.id());
    }
}
