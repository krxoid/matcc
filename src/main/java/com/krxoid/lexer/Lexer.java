package com.krxoid.lexer;

import java.util.*;

public class Lexer {
    private final String source;
    private final List<Token> Tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int TokenColumn = 1;

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("int", TokenType.INT),
            Map.entry("char", TokenType.CHAR),
            Map.entry("void", TokenType.VOID),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("while", TokenType.WHILE),
            Map.entry("for", TokenType.FOR),
            Map.entry("return", TokenType.RETURN),
            Map.entry("break", TokenType.BREAK),
            Map.entry("continue", TokenType.CONTINUE)
    );

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> lex() {

        while (!isAtEnd()) {
            start = current;
            TokenColumn = column;
            scanToken();
        }

        Tokens.add(new Token(
                TokenType.EOF,
                "",
                line,
                column
        ));

        return Tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ', '\r', '\t', '\n' -> {
            }
            case '(' -> add(TokenType.LEFT_PAREN);
            case ')' -> add(TokenType.RIGHT_PAREN);
            case '{' -> add(TokenType.LEFT_BRACE);
            case '}' -> add(TokenType.RIGHT_BRACE);
            case '[' -> add(TokenType.LEFT_BRACKET);
            case ']' -> add(TokenType.RIGHT_BRACKET);
            case ';' -> add(TokenType.SEMICOLON);
            case ',' -> add(TokenType.COMMA);
            case '.' -> add(TokenType.DOT);
            case ':' -> add(TokenType.COLON);
            case '?' -> add(TokenType.QUESTION);
            case '+' -> add(match('+') ? TokenType.PLUS_PLUS : match('=') ? TokenType.PLUS_EQUAL : TokenType.PLUS);
            case '-' -> {
                if (match('>')) add(TokenType.ARROW);
                else add(match('-') ? TokenType.MINUS_MINUS : match('=') ? TokenType.MINUS_EQUAL : TokenType.MINUS);
            }
            case '*' -> add(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR);
            case '%' -> add(match('=') ? TokenType.PERCENT_EQUAL : TokenType.PERCENT);
            case '=' -> add(match('=') ? TokenType.EQUAL_EQUAL : TokenType.ASSIGN);
            case '!' -> add(match('=') ? TokenType.NOT_EQUAL : TokenType.LOGICAL_NOT);
            case '<' -> {
                if (match('<')) add(TokenType.SHIFT_LEFT);
                else add(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            }
            case '>' -> {
                if (match('>')) add(TokenType.SHIFT_RIGHT);
                else add(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            }
            case '&' -> add(match('&') ? TokenType.LOGICAL_AND : TokenType.BIT_AND);
            case '|' -> add(match('|') ? TokenType.LOGICAL_OR : TokenType.BIT_OR);
            case '^' -> add(TokenType.BIT_XOR);
            case '~' -> add(TokenType.BIT_NOT);
            case '/' -> {
                if (match('/')) while (peek() != '\n' && !isAtEnd()) advance();
                else if (match('*')) {
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) advance();
                    if (!isAtEnd()) {
                        advance();
                        advance();
                    }
                } else add(match('=') ? TokenType.SLASH_EQUAL : TokenType.SLASH);
            }
            case '"' -> string();
            case '\'' -> character();
            default -> {
                if (Character.isDigit(c)) number();
                else if (Character.isLetter(c) || c == '_') identifier();
                else throw new RuntimeException("Unexpected: " + c);
            }
        }
    }

    private void number() {
        while (Character.isDigit(peek()))
            advance();

        add(TokenType.INTEGER);
    }

    private void identifier() {
        while (Character.isLetterOrDigit(peek()) || peek() == '_')
            advance();

        String text = source.substring(start, current);

        Tokens.add(new Token(
                KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER),
                text,
                line,
                TokenColumn
        ));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd())
            advance();

        if (isAtEnd())
            throw new RuntimeException("Unterminated string");

        advance(); // consume closing "

        add(TokenType.STRING);
    }

    private void character() {

        if (peek() == '\\') {
            advance();
            advance();
        } else {
            advance();
        }

        if (peek() != '\'')
            throw new RuntimeException("Bad character literal");

        advance(); // consume closing '

        add(TokenType.CHARACTER);
    }

    private void add(TokenType type) {
        Tokens.add(new Token(
                type,
                source.substring(start, current),
                line,
                TokenColumn
        ));
    }

    private char advance() {

        char c = source.charAt(current++);

        if (c == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return c;
    }

    private boolean match(char expected) {

        if (isAtEnd())
            return false;

        if (source.charAt(current) != expected)
            return false;

        current++;
        column++;

        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 >= source.length()
                ? '\0'
                : source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
