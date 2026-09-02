package com.krxoid.ast;

public sealed interface Statement extends BlockItem
        permits BlockStatement,
        ExpressionStatement,
        IfStatement,
        WhileStatement,
        ReturnStatement,
        ForStatement{
}