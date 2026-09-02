package com.krxoid.ast;

public record ForStatement(
        Statement initializer,
        Expression condition,
        Expression increment,
        Statement body
) implements Statement {}
