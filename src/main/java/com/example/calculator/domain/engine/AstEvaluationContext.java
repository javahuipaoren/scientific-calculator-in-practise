package com.example.calculator.domain.engine;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.BinaryOperationNode;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.expression.FunctionCallNode;
import com.example.calculator.domain.expression.IdentifierNode;
import com.example.calculator.domain.expression.NumberNode;
import com.example.calculator.domain.expression.UnaryOperationNode;
import com.example.calculator.domain.rule.BinaryOperatorRule;
import com.example.calculator.domain.rule.FunctionRule;
import com.example.calculator.domain.rule.UnaryOperatorRule;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * 抽象语法树求值上下文。
 *
 * <p>节点遍历流程保持稳定，实际运算委托给规则注册表中的策略。
 * 变量表不可变复制，确保积分和导数等重复求值过程不会污染
 * 外部上下文。
 */
public final class AstEvaluationContext implements EvaluationContext {

    private final CalculationRuleRegistry ruleRegistry;
    private final Map<String, Double> variables;

    public AstEvaluationContext(CalculationRuleRegistry ruleRegistry) {
        this(ruleRegistry, Map.of());
    }

    private AstEvaluationContext(
            CalculationRuleRegistry ruleRegistry,
            Map<String, Double> variables) {
        this.ruleRegistry = Objects.requireNonNull(ruleRegistry);
        this.variables = Map.copyOf(variables);
    }

    @Override
    public double evaluate(ExpressionNode expression) {
        Objects.requireNonNull(expression, "表达式节点不能为空");
        double value;
        if (expression instanceof NumberNode numberNode) {
            value = numberNode.value();
        } else if (expression instanceof IdentifierNode identifierNode) {
            value = evaluateIdentifier(identifierNode);
        } else if (expression instanceof UnaryOperationNode unaryNode) {
            value = evaluateUnaryOperation(unaryNode);
        } else if (expression instanceof BinaryOperationNode binaryNode) {
            value = evaluateBinaryOperation(binaryNode);
        } else if (expression instanceof FunctionCallNode functionNode) {
            value = evaluateFunction(functionNode);
        } else {
            throw new InvalidExpressionException("unsupported expression node");
        }
        return requireFinite(value);
    }

    @Override
    public EvaluationContext withVariable(String variableName, double value) {
        requireFinite(value);
        String normalizedName = normalize(variableName);
        Map<String, Double> newVariables = new HashMap<>(variables);
        newVariables.put(normalizedName, value);
        return new AstEvaluationContext(ruleRegistry, newVariables);
    }

    private double evaluateIdentifier(IdentifierNode node) {
        String name = normalize(node.name());
        Double variableValue = variables.get(name);
        if (variableValue != null) {
            return variableValue;
        }
        return ruleRegistry.findConstant(name)
                .map(constant -> constant.value())
                .orElseThrow(() -> new InvalidExpressionException(
                        "unknown identifier: " + name));
    }

    private double evaluateUnaryOperation(UnaryOperationNode node) {
        UnaryOperatorRule rule =
                ruleRegistry.requireUnaryOperator(node.operator());
        return rule.apply(evaluate(node.operand()));
    }

    private double evaluateBinaryOperation(BinaryOperationNode node) {
        BinaryOperatorRule rule =
                ruleRegistry.requireBinaryOperator(node.operator());
        return rule.apply(evaluate(node.left()), evaluate(node.right()));
    }

    private double evaluateFunction(FunctionCallNode node) {
        FunctionRule rule = ruleRegistry.requireFunction(node.name());
        return rule.apply(node.arguments(), this);
    }

    private double requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new InvalidExpressionException(
                    "non-finite intermediate value or invalid real operation");
        }
        return value;
    }

    private String normalize(String name) {
        return Objects.requireNonNull(name, "变量名称不能为空")
                .toLowerCase(Locale.ROOT);
    }
}
