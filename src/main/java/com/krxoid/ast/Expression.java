package com.krxoid.ast;

public sealed interface Expression permits
        AssignmentExpression,
        BinaryExpression,
        CallExpression,
        LiteralExpression,
        UnaryExpression,
        VariableExpression {
}