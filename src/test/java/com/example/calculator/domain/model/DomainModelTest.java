package com.example.calculator.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.calculator.domain.exception.InvalidExpressionException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 领域模型不变条件测试。
 */
@DisplayName("计算领域模型")
class DomainModelTest {

    @Test
    @DisplayName("计算聚合根应完整保存值对象")
    void shouldCreateCompletedCalculation() {
        CalculationId id = CalculationId.of(1L);
        MathematicalExpression expression = MathematicalExpression.of("1 + 2");
        CalculationResult result = CalculationResult.of(3.0d);
        Instant calculatedAt = Instant.parse("2026-09-15T00:00:00Z");

        Calculation calculation =
                Calculation.completed(id, expression, result, calculatedAt);

        assertEquals(id, calculation.id());
        assertEquals(expression, calculation.expression());
        assertEquals(result, calculation.result());
        assertEquals(calculatedAt, calculation.calculatedAt());
    }

    @Test
    @DisplayName("值对象应保护领域不变条件")
    void shouldProtectValueObjectInvariants() {
        assertThrows(IllegalArgumentException.class,
                () -> CalculationId.of(0L));
        assertThrows(InvalidExpressionException.class,
                () -> MathematicalExpression.of("  "));
        assertThrows(InvalidExpressionException.class,
                () -> CalculationResult.of(Double.NaN));
    }
}
