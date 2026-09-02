package com.krxoid.ast;

import java.util.List;

public record FunctionDeclaration(
        String name,
        Type returnType,
        List<VariableDeclaration> parameters,
        BlockStatement body
) implements Declaration {}