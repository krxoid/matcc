package com.krxoid.ast;

public record ReturnStatement(
        Expression value
) implements Statement {
}
