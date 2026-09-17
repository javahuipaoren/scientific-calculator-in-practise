package com.example.calculator.application.service;

import com.example.calculator.application.command.CalculateExpressionCommand;
import com.example.calculator.application.result.CalculationView;
import com.example.calculator.domain.model.Calculation;
import com.example.calculator.domain.model.CalculationResult;
import com.example.calculator.domain.model.MathematicalExpression;
import com.example.calculator.domain.repository.CalculationHistoryRepository;
import com.example.calculator.domain.service.CalculationIdGenerator;
import com.example.calculator.domain.service.ExpressionCalculator;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 计算用例应用服务。
 *
 * <p>应用层负责编排领域对象和仓储，不包含表达式解析等核心领域规则。
 */
public final class CalculationApplicationService {

    private final ExpressionCalculator expressionCalculator;
    private final CalculationHistoryRepository historyRepository;
    private final CalculationIdGenerator idGenerator;
    private final Clock clock;

    public CalculationApplicationService(
            ExpressionCalculator expressionCalculator,
            CalculationHistoryRepository historyRepository,
            CalculationIdGenerator idGenerator,
            Clock clock) {
        this.expressionCalculator = Objects.requireNonNull(expressionCalculator);
        this.historyRepository = Objects.requireNonNull(historyRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * 执行表达式计算并保存历史记录。
     *
     * @param command 计算命令
     * @return 已完成的计算记录视图
     */
    public CalculationView calculate(CalculateExpressionCommand command) {
        Objects.requireNonNull(command, "计算命令不能为空");

        MathematicalExpression expression =
                MathematicalExpression.of(command.expression());
        CalculationResult result = expressionCalculator.calculate(expression);
        Instant calculatedAt = clock.instant();

        Calculation calculation = Calculation.completed(
                idGenerator.nextId(), expression, result, calculatedAt);
        historyRepository.save(calculation);
        return CalculationView.from(calculation);
    }

    /**
     * 查询当前进程保留的计算历史。
     *
     * @return 按计算顺序排列的只读视图
     */
    public List<CalculationView> findHistory() {
        return historyRepository.findAll().stream()
                .map(CalculationView::from)
                .toList();
    }

    /** 清空当前进程中的计算历史。 */
    public void clearHistory() {
        historyRepository.clear();
    }
}
