package com.krxoid.lexer;

//All Token types supported by the compiler.
//This is a subset of C intended for the BatPU-2 compiler.
public enum TokenType {

    // ===== Literals =====
    IDENTIFIER,
    INTEGER,
    STRING,
    CHARACTER,

    // ===== Keywords =====
    INT,
    CHAR,
    VOID,

    IF,
    ELSE,

    WHILE,
    FOR,

    RETURN,

    BREAK,
    CONTINUE,

    // ===== Arithmetic =====
    PLUS,               // +
    MINUS,              // -
    STAR,               // *
    SLASH,              // /
    PERCENT,            // %

    // ===== Assignment =====
    ASSIGN,             // =
    PLUS_EQUAL,         // +=
    MINUS_EQUAL,        // -=
    STAR_EQUAL,         // *=
    SLASH_EQUAL,        // /=
    PERCENT_EQUAL,      // %=

    // ===== Unary =====
    PLUS_PLUS,          // ++
    MINUS_MINUS,        // --
    LOGICAL_NOT,        // !
    BIT_NOT,            // ~

    // ===== Comparison =====
    EQUAL_EQUAL,        // ==
    NOT_EQUAL,          // !=
    LESS,               // <
    LESS_EQUAL,         // <=
    GREATER,            // >
    GREATER_EQUAL,      // >=

    // ===== Logical =====
    LOGICAL_AND,        // &&
    LOGICAL_OR,         // ||

    // ===== Bitwise =====
    BIT_AND,            // &
    BIT_OR,             // |
    BIT_XOR,            // ^
    SHIFT_LEFT,         // <<
    SHIFT_RIGHT,        // >>

    // ===== Delimiters =====
    LEFT_PAREN,         // (
    RIGHT_PAREN,        // )

    LEFT_BRACE,         // {
    RIGHT_BRACE,        // }

    LEFT_BRACKET,       // [
    RIGHT_BRACKET,      // ]

    COMMA,              // ,
    DOT,                // .
    COLON,              // :
    SEMICOLON,          // ;
    QUESTION,           // ?

    ARROW,              // ->

    // ===== Special =====
    EOF
}