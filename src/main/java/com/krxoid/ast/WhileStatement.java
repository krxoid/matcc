package com.krxoid.ast;

public record WhileStatement(
        Expression condition,
        Statement body
) implements Statement {
}
