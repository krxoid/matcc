package com.krxoid.ast;

public record AssignmentExpression(
        String name,
        Expression value
) implements Expression {
}
