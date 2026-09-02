package com.krxoid.semantic;

import com.krxoid.ast.FunctionDeclaration;
import com.krxoid.ast.Type;

public record Symbol(
        String name,
        Type type,
        Kind kind,
        FunctionDeclaration function
) {

    public enum Kind {
        VARIABLE,
        PARAMETER,
        FUNCTION
    }
}