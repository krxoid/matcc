package com.krxoid.ast;

public record LiteralExpression(
        Object value
) implements Expression {
}
