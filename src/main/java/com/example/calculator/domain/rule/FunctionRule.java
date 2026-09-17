package com.example.calculator.domain.rule;

import com.example.calculator.domain.engine.EvaluationContext;
import com.example.calculator.domain.expression.ExpressionNode;
import java.util.List;

/**
 * 数学函数策略扩展点。
 *
 * <p>函数接收未求值的表达式树和求值上下文，因此不仅能实现普通
 * 单参数函数，也能实现需要重复求值和变量绑定的积分、导数等
 * 高级规则。
 */
public interface FunctionRule {

    /** 函数名称，不区分大小写。 */
    String name();

    /**
     * 执行函数计算。
     *
     * @param arguments 未求值的参数表达式树
     * @param context 当前求值上下文
     * @return 函数计算结果
     */
    double apply(List<ExpressionNode> arguments, EvaluationContext context);
}
