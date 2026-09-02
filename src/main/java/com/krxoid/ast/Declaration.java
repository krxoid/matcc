package com.krxoid.ast;

public sealed interface Declaration extends BlockItem
        permits FunctionDeclaration, VariableDeclaration {
}