package com.krxoid.ast;

public record VariableDeclaration(
        Type type,
        String name,
        Expression initializer
) implements Declaration {
}