package com.example.calculator.domain.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.model.MathematicalExpression;
import com.example.calculator.domain.rule.FunctionRule;
import com.example.calculator.domain.service.ExpressionCalculator;
import com.example.calculator.support.TestCalculatorFactory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 计算模板、规则策略和扩展能力的领域测试。 */
@DisplayName("可扩展表达式计算模板")
class ExpressionCalculationTemplateTest {

    private final ExpressionCalculator calculator =
            TestCalculatorFactory.createCalculator();

    @Test
    @DisplayName("应按优先级执行基础四则和取模运算")
    void shouldCalculateBasicOperatorsByPrecedence() {
        assertEquals(11.0d, calculate("2 + 3 * 4 - 6 / 2"));
        assertEquals(1.0d, calculate("10 % 3"));
    }

    @Test
    @DisplayName("幂运算应右结合且优先级高于一元负号")
    void shouldApplyPowerAssociativityAndUnaryPrecedence() {
        assertEquals(512.0d, calculate("2^3^2"));
        assertEquals(-4.0d, calculate("-2^2"));
        assertEquals(0.25d, calculate("2^-2"));
    }

    @Test
    @DisplayName("应支持科学函数、数学常量和科学计数法")
    void shouldCalculateScientificFunctionsAndConstants() {
        assertEquals(4.0d, calculate("sqrt(9) + sin(30) * 2"), 1.0e-12d);
        assertEquals(Math.PI + Math.E, calculate("pi + e"), 1.0e-12d);
        assertEquals(200.001d, calculate("2e2 + 1e-3"), 1.0e-12d);
        assertEquals(2.0d, calculate("log(100)"), 1.0e-12d);
        assertEquals(1.0d, calculate("ln(e)"), 1.0e-12d);
    }

    @Test
    @DisplayName("定义域错误和未知规则应抛出领域异常")
    void shouldRejectInvalidDomainAndUnknownRules() {
        assertThrows(InvalidExpressionException.class,
                () -> calculate("1 / 0"));
        assertThrows(InvalidExpressionException.class,
                () -> calculate("sqrt(-1)"));
        assertThrows(InvalidExpressionException.class,
                () -> calculate("unknown(1)"));
        assertThrows(InvalidExpressionException.class,
                () -> calculate("missing + 1"));
    }

    @Test
    @DisplayName("应通过独立策略完成定积分和导数计算")
    void shouldCalculateIntegralAndDerivativeWithStrategies() {
        assertEquals(1.0d / 3.0d,
                calculate("integral(x^2, 0, 1)"), 1.0e-9d);
        assertEquals(12.0d,
                calculate("derivative(x^3, 2)"), 1.0e-6d);
        assertEquals(-0.5d,
                calculate("integral(x, 1, 0)"), 1.0e-9d);
    }

    @Test
    @DisplayName("新增函数策略时不应修改计算模板")
    void shouldExtendFunctionWithoutChangingTemplate() {
        FunctionRule cubeRule = new FunctionRule() {
            @Override
            public String name() {
                return "cube";
            }

            @Override
            public double apply(
                    List<ExpressionNode> arguments,
                    EvaluationContext context) {
                if (arguments.size() != 1) {
                    throw new InvalidExpressionException(
                            "cube expects exactly 1 argument");
                }
                double value = context.evaluate(arguments.get(0));
                return value * value * value;
            }
        };
        ExpressionCalculator extensibleCalculator =
                TestCalculatorFactory.createCalculator(List.of(cubeRule));

        double result = extensibleCalculator.calculate(
                MathematicalExpression.of("cube(3) + 1")).value();

        assertEquals(28.0d, result);
    }

    @Test
    @DisplayName("重复规则注册应在启动阶段快速失败")
    void shouldRejectDuplicateRules() {
        FunctionRule duplicateSqrt = new FunctionRule() {
            @Override
            public String name() {
                return "SQRT";
            }

            @Override
            public double apply(
                    List<ExpressionNode> arguments,
                    EvaluationContext context) {
                return 0.0d;
            }
        };

        assertThrows(IllegalArgumentException.class,
                () -> TestCalculatorFactory.createCalculator(
                        List.of(duplicateSqrt)));
    }

    private double calculate(String expression) {
        return calculator.calculate(MathematicalExpression.of(expression))
                .value();
    }
}
