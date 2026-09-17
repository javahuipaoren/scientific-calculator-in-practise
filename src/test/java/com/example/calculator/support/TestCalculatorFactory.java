package com.example.calculator.support;

import com.example.calculator.domain.engine.CalculationRuleRegistry;
import com.example.calculator.domain.engine.ExpressionCalculationTemplate;
import com.example.calculator.domain.rule.BinaryOperatorRule;
import com.example.calculator.domain.rule.ConstantRule;
import com.example.calculator.domain.rule.FunctionRule;
import com.example.calculator.domain.rule.UnaryOperatorRule;
import com.example.calculator.domain.rule.calculus.DefiniteIntegralFunctionRule;
import com.example.calculator.domain.rule.calculus.DerivativeFunctionRule;
import com.example.calculator.domain.rule.constant.EulerConstantRule;
import com.example.calculator.domain.rule.constant.PiConstantRule;
import com.example.calculator.domain.rule.function.AbsoluteFunctionRule;
import com.example.calculator.domain.rule.function.CeilingFunctionRule;
import com.example.calculator.domain.rule.function.CommonLogarithmFunctionRule;
import com.example.calculator.domain.rule.function.CosineFunctionRule;
import com.example.calculator.domain.rule.function.ExponentialFunctionRule;
import com.example.calculator.domain.rule.function.FloorFunctionRule;
import com.example.calculator.domain.rule.function.NaturalLogarithmFunctionRule;
import com.example.calculator.domain.rule.function.SineFunctionRule;
import com.example.calculator.domain.rule.function.SquareRootFunctionRule;
import com.example.calculator.domain.rule.function.TangentFunctionRule;
import com.example.calculator.domain.rule.operator.AddOperatorRule;
import com.example.calculator.domain.rule.operator.DivideOperatorRule;
import com.example.calculator.domain.rule.operator.ModuloOperatorRule;
import com.example.calculator.domain.rule.operator.MultiplyOperatorRule;
import com.example.calculator.domain.rule.operator.PowerOperatorRule;
import com.example.calculator.domain.rule.operator.SubtractOperatorRule;
import com.example.calculator.domain.rule.operator.UnaryMinusOperatorRule;
import com.example.calculator.domain.rule.operator.UnaryPlusOperatorRule;
import com.example.calculator.domain.service.ExpressionCalculator;
import java.util.ArrayList;
import java.util.List;

/** 测试用内置计算规则工厂，避免各测试重复装配领域对象。 */
public final class TestCalculatorFactory {

    private TestCalculatorFactory() {
    }

    /** 创建包含全部内置规则的表达式计算器。 */
    public static ExpressionCalculator createCalculator() {
        return createCalculator(List.of());
    }

    /** 创建包含额外函数策略的表达式计算器。 */
    public static ExpressionCalculator createCalculator(
            List<FunctionRule> additionalFunctions) {
        List<FunctionRule> functionRules = new ArrayList<>(List.of(
                new SquareRootFunctionRule(),
                new AbsoluteFunctionRule(),
                new SineFunctionRule(),
                new CosineFunctionRule(),
                new TangentFunctionRule(),
                new CommonLogarithmFunctionRule(),
                new NaturalLogarithmFunctionRule(),
                new ExponentialFunctionRule(),
                new FloorFunctionRule(),
                new CeilingFunctionRule(),
                new DefiniteIntegralFunctionRule(),
                new DerivativeFunctionRule()));
        functionRules.addAll(additionalFunctions);

        CalculationRuleRegistry registry = new CalculationRuleRegistry(
                binaryOperatorRules(),
                unaryOperatorRules(),
                functionRules,
                constantRules());
        return new ExpressionCalculationTemplate(registry);
    }

    private static List<BinaryOperatorRule> binaryOperatorRules() {
        return List.of(
                new AddOperatorRule(),
                new SubtractOperatorRule(),
                new MultiplyOperatorRule(),
                new DivideOperatorRule(),
                new ModuloOperatorRule(),
                new PowerOperatorRule());
    }

    private static List<UnaryOperatorRule> unaryOperatorRules() {
        return List.of(
                new UnaryPlusOperatorRule(),
                new UnaryMinusOperatorRule());
    }

    private static List<ConstantRule> constantRules() {
        return List.of(
                new PiConstantRule(),
                new EulerConstantRule());
    }
}
