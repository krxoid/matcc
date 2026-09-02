package com.krxoid.ast;

public record IfStatement(
        Expression condition,
        Statement thenBranch,
        Statement elseBranch
) implements Statement {
}
