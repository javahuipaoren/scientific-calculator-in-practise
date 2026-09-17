package com.example.calculator.domain.engine;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.rule.BinaryOperatorRule;
import com.example.calculator.domain.rule.ConstantRule;
import com.example.calculator.domain.rule.FunctionRule;
import com.example.calculator.domain.rule.UnaryOperatorRule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 计算规则注册表。
 *
 * <p>注册表集中管理运算符、函数和常量策略，并在启动阶段检查
 * 重复规则。计算模板只依赖注册表，不依赖任何具体计算方法，从而
 * 满足依赖倒置原则和开闭原则。
 */
public final class CalculationRuleRegistry {

    private final List<BinaryOperatorRule> binaryOperators;
    private final List<UnaryOperatorRule> unaryOperators;
    private final Map<String, BinaryOperatorRule> binaryOperatorMap;
    private final Map<String, UnaryOperatorRule> unaryOperatorMap;
    private final Map<String, FunctionRule> functionMap;
    private final Map<String, ConstantRule> constantMap;

    public CalculationRuleRegistry(
            Collection<BinaryOperatorRule> binaryOperators,
            Collection<UnaryOperatorRule> unaryOperators,
            Collection<FunctionRule> functions,
            Collection<ConstantRule> constants) {
        this.binaryOperatorMap = uniqueMap(
                binaryOperators,
                BinaryOperatorRule::symbol,
                "二元运算符");
        this.unaryOperatorMap = uniqueMap(
                unaryOperators,
                UnaryOperatorRule::symbol,
                "一元运算符");
        this.functionMap = uniqueMap(
                functions,
                FunctionRule::name,
                "函数");
        this.constantMap = uniqueMap(
                constants,
                ConstantRule::name,
                "常量");
        this.binaryOperators = longestSymbolFirst(
                binaryOperatorMap.values(), BinaryOperatorRule::symbol);
        this.unaryOperators = longestSymbolFirst(
                unaryOperatorMap.values(), UnaryOperatorRule::symbol);
    }

    /** 在指定位置查找已注册的二元运算策略。 */
    public Optional<BinaryOperatorRule> matchBinaryOperator(
            String expression,
            int position) {
        return binaryOperators.stream()
                .filter(rule -> expression.startsWith(
                        rule.symbol(), position))
                .findFirst();
    }

    /** 在指定位置查找已注册的一元运算策略。 */
    public Optional<UnaryOperatorRule> matchUnaryOperator(
            String expression,
            int position) {
        return unaryOperators.stream()
                .filter(rule -> expression.startsWith(
                        rule.symbol(), position))
                .findFirst();
    }

    /** 根据符号获取二元运算策略。 */
    public BinaryOperatorRule requireBinaryOperator(String symbol) {
        BinaryOperatorRule rule = binaryOperatorMap.get(normalize(symbol));
        if (rule == null) {
            throw new InvalidExpressionException(
                    "unsupported binary operator: " + symbol);
        }
        return rule;
    }

    /** 根据符号获取一元运算策略。 */
    public UnaryOperatorRule requireUnaryOperator(String symbol) {
        UnaryOperatorRule rule = unaryOperatorMap.get(normalize(symbol));
        if (rule == null) {
            throw new InvalidExpressionException(
                    "unsupported unary operator: " + symbol);
        }
        return rule;
    }

    /** 根据名称获取函数策略。 */
    public FunctionRule requireFunction(String name) {
        FunctionRule rule = functionMap.get(normalize(name));
        if (rule == null) {
            throw new InvalidExpressionException(
                    "unsupported function: " + name);
        }
        return rule;
    }

    /** 根据名称查找数学常量。 */
    public Optional<ConstantRule> findConstant(String name) {
        return Optional.ofNullable(constantMap.get(normalize(name)));
    }

    private static <T> Map<String, T> uniqueMap(
            Collection<T> rules,
            Function<T, String> keyExtractor,
            String ruleType) {
        Objects.requireNonNull(rules, ruleType + "集合不能为空");
        Map<String, T> result = new LinkedHashMap<>();
        for (T rule : rules) {
            Objects.requireNonNull(rule, ruleType + "不能为空");
            String key = normalize(keyExtractor.apply(rule));
            if (key.isBlank()) {
                throw new IllegalArgumentException(
                        ruleType + "名称不能为空");
            }
            if (result.putIfAbsent(key, rule) != null) {
                throw new IllegalArgumentException(
                        "存在重复的" + ruleType + "规则: " + key);
            }
        }
        return Map.copyOf(result);
    }

    private static <T> List<T> longestSymbolFirst(
            Collection<T> rules,
            Function<T, String> symbolExtractor) {
        List<T> result = new ArrayList<>(rules);
        result.sort(Comparator.comparingInt(
                        (T rule) -> symbolExtractor.apply(rule).length())
                .reversed());
        return List.copyOf(result);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "规则名称不能为空")
                .toLowerCase(Locale.ROOT);
    }
}
