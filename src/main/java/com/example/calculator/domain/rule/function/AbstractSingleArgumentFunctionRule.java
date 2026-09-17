package com.example.calculator.domain.rule.function;

import com.example.calculator.domain.engine.EvaluationContext;
import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.rule.FunctionRule;
import java.util.List;

/**
 * 单参数数学函数的通用模板。
 *
 * <p>该模板统一完成参数数量校验和参数求值，具体函数只实现纯数学
 * 计算逻辑。
 */
public abstract class AbstractSingleArgumentFunctionRule
        implements FunctionRule {

    @Override
    public final double apply(
            List<ExpressionNode> arguments,
            EvaluationContext context) {
        if (arguments.size() != 1) {
            throw new InvalidExpressionException(
                    name() + " expects exactly 1 argument");
        }
        return calculate(context.evaluate(arguments.get(0)));
    }

    /** 执行具体单参数函数运算。 */
    protected abstract double calculate(double argument);
}
