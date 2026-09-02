package com.krxoid.ast;

import java.util.List;

public record CallExpression(
        String functionName,
        List<Expression> arguments
) implements Expression {}