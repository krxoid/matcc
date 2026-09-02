package com.krxoid.lexer;

import com.krxoid.lexer.TokenType;

import java.util.Objects;

//Represents a single Token produced by the lexer
public record Token(
        TokenType type,
        String lexeme,
        int line,
        int column
) {

    public Token {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(lexeme, "lexeme");

        if (line < 1)
            throw new IllegalArgumentException("line must be >= 1");

        if (column < 1)
            throw new IllegalArgumentException("column must be >= 1");
    }

    @Override
    public String toString() {
        return String.format(
                "%-18s '%s' (%d:%d)",
                type,
                escape(lexeme),
                line,
                column
        );
    }

    private static String escape(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}