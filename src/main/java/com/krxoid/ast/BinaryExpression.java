package com.krxoid.ast;

import com.krxoid.ast.Expression;
import com.krxoid.lexer.Token;

public record BinaryExpression(
        Expression left,
        Token operator,
        Expression right
) implements Expression {
}