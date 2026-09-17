package com.example.calculator.domain.engine;

import com.example.calculator.domain.exception.InvalidExpressionException;
import com.example.calculator.domain.expression.BinaryOperationNode;
import com.example.calculator.domain.expression.ExpressionNode;
import com.example.calculator.domain.expression.FunctionCallNode;
import com.example.calculator.domain.expression.IdentifierNode;
import com.example.calculator.domain.expression.NumberNode;
import com.example.calculator.domain.expression.UnaryOperationNode;
import com.example.calculator.domain.rule.Associativity;
import com.example.calculator.domain.rule.BinaryOperatorRule;
import com.example.calculator.domain.rule.UnaryOperatorRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于 Pratt 算法的可扩展表达式解析器。
 *
 * <p>解析器从规则注册表读取运算符优先级和结合方向。新增运算策略后，
 * 无需在解析器中增加对应的条件分支。
 */
public final class PrattExpressionParser {

    private static final int MAX_NESTING_DEPTH = 64;

    private final String input;
    private final CalculationRuleRegistry ruleRegistry;
    private int position;
    private int depth;

    public PrattExpressionParser(
            String input, CalculationRuleRegistry ruleRegistry) {
        this.input = Objects.requireNonNull(input, "表达式不能为空");
        this.ruleRegistry = Objects.requireNonNull(
                ruleRegistry, "规则注册表不能为空");
    }

    /**
     * 将表达式文本解析为抽象语法树。
     *
     * @return 表达式树根节点
     */
    public ExpressionNode parse() {
        ExpressionNode expression = parseExpression(0);
        skipWhitespace();
        if (!atEnd()) {
            throw error("unexpected token");
        }
        return expression;
    }

    private ExpressionNode parseExpression(int minimumPrecedence) {
        enterNestedLevel();
        try {
            ExpressionNode left = parsePrefixExpression();
            while (true) {
                skipWhitespace();
                Optional<BinaryOperatorRule> matched =
                        ruleRegistry.matchBinaryOperator(input, position);
                if (matched.isEmpty()) {
                    return left;
                }

                BinaryOperatorRule rule = matched.get();
                if (rule.precedence() < minimumPrecedence) {
                    return left;
                }

                position += rule.symbol().length();
                int rightPrecedence = rule.associativity() == Associativity.LEFT
                        ? rule.precedence() + 1
                        : rule.precedence();
                ExpressionNode right = parseExpression(rightPrecedence);
                left = new BinaryOperationNode(rule.symbol(), left, right);
            }
        } finally {
            depth--;
        }
    }

    private ExpressionNode parsePrefixExpression() {
        skipWhitespace();
        Optional<UnaryOperatorRule> matched =
                ruleRegistry.matchUnaryOperator(input, position);
        if (matched.isPresent()) {
            UnaryOperatorRule rule = matched.get();
            position += rule.symbol().length();
            ExpressionNode operand = parseExpression(rule.precedence());
            return new UnaryOperationNode(rule.symbol(), operand);
        }
        return parsePrimaryExpression();
    }

    private ExpressionNode parsePrimaryExpression() {
        skipWhitespace();
        if (matchCharacter('(')) {
            ExpressionNode expression = parseExpression(0);
            requireCharacter(')', "missing ')'");
            return expression;
        }
        if (!atEnd() && (Character.isDigit(peek()) || peek() == '.')) {
            return parseNumber();
        }
        if (!atEnd() && isIdentifierStart(peek())) {
            return parseIdentifierOrFunction();
        }
        throw error("operand expected");
    }

    private ExpressionNode parseIdentifierOrFunction() {
        String name = parseIdentifier();
        skipWhitespace();
        if (!matchCharacter('(')) {
            return new IdentifierNode(name);
        }

        List<ExpressionNode> arguments = new ArrayList<>();
        skipWhitespace();
        if (!matchCharacter(')')) {
            do {
                arguments.add(parseExpression(0));
                skipWhitespace();
            } while (matchCharacter(','));
            requireCharacter(')', "missing ')'");
        }
        return new FunctionCallNode(name, arguments);
    }

    private ExpressionNode parseNumber() {
        int start = position;
        consumeDigits();
        if (matchCharacterRaw('.')) {
            consumeDigits();
        }
        if (matchCharacterRaw('e', 'E')) {
            matchCharacterRaw('+', '-');
            int exponentStart = position;
            consumeDigits();
            if (position == exponentStart) {
                throw error("invalid scientific exponent");
            }
        }

        try {
            double value = Double.parseDouble(input.substring(start, position));
            if (!Double.isFinite(value)) {
                throw error("number is not finite");
            }
            return new NumberNode(value);
        } catch (NumberFormatException exception) {
            throw error("invalid number");
        }
    }

    private String parseIdentifier() {
        int start = position;
        position++;
        while (!atEnd() && isIdentifierPart(peek())) {
            position++;
        }
        return input.substring(start, position).toLowerCase(Locale.ROOT);
    }

    private void consumeDigits() {
        while (!atEnd() && Character.isDigit(peek())) {
            position++;
        }
    }

    private boolean isIdentifierStart(char character) {
        return Character.isLetter(character) || character == '_';
    }

    private boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }

    private void enterNestedLevel() {
        depth++;
        if (depth > MAX_NESTING_DEPTH) {
            throw error(
                    "expression nesting exceeds " + MAX_NESTING_DEPTH + " levels");
        }
    }

    private void skipWhitespace() {
        while (!atEnd() && Character.isWhitespace(peek())) {
            position++;
        }
    }

    private boolean matchCharacter(char... expectedCharacters) {
        skipWhitespace();
        return matchCharacterRaw(expectedCharacters);
    }

    private boolean matchCharacterRaw(char... expectedCharacters) {
        if (atEnd()) {
            return false;
        }
        for (char expected : expectedCharacters) {
            if (peek() == expected) {
                position++;
                return true;
            }
        }
        return false;
    }

    private void requireCharacter(char expected, String message) {
        if (!matchCharacter(expected)) {
            throw error(message);
        }
    }

    private char peek() {
        return input.charAt(position);
    }

    private boolean atEnd() {
        return position >= input.length();
    }

    private InvalidExpressionException error(String message) {
        return new InvalidExpressionException(message + " at position " + position);
    }
}
