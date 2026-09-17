package com.example.calculator.domain.engine;

import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.model.CalculationResult;
import com.example.calculator.domain.model.MathematicalExpression;
import com.example.calculator.domain.service.ExpressionCalculator;
import java.util.Objects;

/**
 * 表达式计算模板。
 *
 * <p>设计方式类似 {@code JdbcTemplate}：模板固定“解析、求值、
 * 结果封装”的通用流程，运算符、函数和常量的差异行为由注入的策略
 * 规则完成。增加新规则无需修改本类。
 */
public final class ExpressionCalculationTemplate implements ExpressionCalculator {

    private final CalculationRuleRegistry ruleRegistry;

    public ExpressionCalculationTemplate(
            CalculationRuleRegistry ruleRegistry) {
        this.ruleRegistry = Objects.requireNonNull(ruleRegistry);
    }

    @Override
    public CalculationResult calculate(MathematicalExpression expression) {
        Objects.requireNonNull(expression, "数学表达式不能为空");

        ExpressionNode syntaxTree = new PrattExpressionParser(
                expression.value(), ruleRegistry).parse();
        EvaluationContext evaluationContext =
                new AstEvaluationContext(ruleRegistry);
        double result = evaluationContext.evaluate(syntaxTree);
        return CalculationResult.of(result);
    }
}
