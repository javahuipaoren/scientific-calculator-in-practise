package com.example.calculator.infrastructure.config;

import com.example.calculator.application.service.CalculationApplicationService;
import com.example.calculator.domain.engine.CalculationRuleRegistry;
import com.example.calculator.domain.engine.ExpressionCalculationTemplate;
import com.example.calculator.domain.repository.CalculationHistoryRepository;
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
import com.example.calculator.domain.service.CalculationIdGenerator;
import com.example.calculator.domain.service.ExpressionCalculator;
import com.example.calculator.infrastructure.logging.HttpAccessLogStore;
import com.example.calculator.infrastructure.logging.InMemoryHttpAccessLogStore;
import com.example.calculator.infrastructure.persistence.AtomicCalculationIdGenerator;
import com.example.calculator.infrastructure.persistence.InMemoryCalculationHistoryRepository;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * DDD 各层对象和内置计算策略的依赖装配配置。
 *
 * <p>领域层和应用层不依赖 Spring。基础设施层通过 {@link Import}
 * 将无框架注解的领域策略注册为 Bean，再由规则注册表统一发现，实现
 * “模板流程稳定、规则按需扩展”。
 */
@Configuration
@Import({
    AddOperatorRule.class,
    SubtractOperatorRule.class,
    MultiplyOperatorRule.class,
    DivideOperatorRule.class,
    ModuloOperatorRule.class,
    PowerOperatorRule.class,
    UnaryPlusOperatorRule.class,
    UnaryMinusOperatorRule.class,
    SquareRootFunctionRule.class,
    AbsoluteFunctionRule.class,
    SineFunctionRule.class,
    CosineFunctionRule.class,
    TangentFunctionRule.class,
    CommonLogarithmFunctionRule.class,
    NaturalLogarithmFunctionRule.class,
    ExponentialFunctionRule.class,
    FloorFunctionRule.class,
    CeilingFunctionRule.class,
    DefiniteIntegralFunctionRule.class,
    DerivativeFunctionRule.class,
    PiConstantRule.class,
    EulerConstantRule.class
})
public class CalculatorBeanConfiguration {

    /** 内存历史记录最大保留数量。 */
    public static final int HISTORY_CAPACITY = 1000;

    /**
     * 汇集所有规则 Bean 并创建只读注册表。
     *
     * <p>新增策略只需实现对应小接口并在组合根中注册，不需要修改
     * 解析器、求值器或计算模板。
     */
    @Bean
    CalculationRuleRegistry calculationRuleRegistry(
            List<BinaryOperatorRule> binaryOperatorRules,
            List<UnaryOperatorRule> unaryOperatorRules,
            List<FunctionRule> functionRules,
            List<ConstantRule> constantRules) {
        return new CalculationRuleRegistry(
                binaryOperatorRules,
                unaryOperatorRules,
                functionRules,
                constantRules);
    }

    @Bean
    ExpressionCalculator expressionCalculator(
            CalculationRuleRegistry ruleRegistry) {
        return new ExpressionCalculationTemplate(ruleRegistry);
    }

    /** 创建有界的进程内访问日志存储，避免日志记录无限占用堆内存。 */
    @Bean
    HttpAccessLogStore httpAccessLogStore(
            @Value("${calculator.access-log.capacity:1000}") int capacity) {
        return new InMemoryHttpAccessLogStore(capacity);
    }

    @Bean
    CalculationHistoryRepository calculationHistoryRepository() {
        return new InMemoryCalculationHistoryRepository(HISTORY_CAPACITY);
    }

    @Bean
    CalculationIdGenerator calculationIdGenerator() {
        return new AtomicCalculationIdGenerator();
    }

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    CalculationApplicationService calculationApplicationService(
            ExpressionCalculator expressionCalculator,
            CalculationHistoryRepository historyRepository,
            CalculationIdGenerator idGenerator,
            Clock clock) {
        return new CalculationApplicationService(
                expressionCalculator,
                historyRepository,
                idGenerator,
                clock);
    }
}
