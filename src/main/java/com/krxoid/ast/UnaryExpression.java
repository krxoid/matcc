package com.krxoid.ast;

import com.krxoid.ast.Expression;
import com.krxoid.lexer.Token;

public record UnaryExpression(
        Token operator,
        Expression operand
) implements Expression {
}