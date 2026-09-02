package com.krxoid.ast;

public record ExpressionStatement(
        Expression expression
) implements Statement {
}
