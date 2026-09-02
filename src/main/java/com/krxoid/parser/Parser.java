package com.krxoid.parser;

import com.krxoid.ast.*;
import com.krxoid.lexer.Token;
import com.krxoid.lexer.TokenType;

import java.util.*;

public class Parser {
    private final List<Token> Tokens;
    private int current;

    public Parser(List<Token> Tokens){ this.Tokens=Tokens; }

    public Program parse() {
        List<Declaration> Declarations = new ArrayList<>();
        while(!isAtEnd()){
            Declarations.add(parseDeclaration());
        }
        return new Program(Declarations);
    }

    private Declaration parseDeclaration() {

        Type type = parseType();

        Token name = consume(
                TokenType.IDENTIFIER,
                "Expected identifier"
        );

        if (match(TokenType.LEFT_PAREN))
            return parseFunction(type, name.lexeme());

        Expression initializer = null;

        if (match(TokenType.ASSIGN))
            initializer = parseExpression();

        consume(
                TokenType.SEMICOLON,
                "Expected ';'"
        );

        return new VariableDeclaration(
                type,
                name.lexeme(),
                initializer
        );
    }

    private FunctionDeclaration parseFunction(Type type,String name){
        List<VariableDeclaration> params=new ArrayList<>();
        if(!check(TokenType.RIGHT_PAREN)){
            do{
                Type pt = parseType();
                Token pn = consume(TokenType.IDENTIFIER,"Expected parameter");
                params.add(new VariableDeclaration(pt,pn.lexeme(),null));
            }while(match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')'");
        consume(TokenType.LEFT_BRACE, "Expected '{'");

        BlockStatement body = parseBlock();

        return new FunctionDeclaration(
                name,
                type,
                params,
                body
        );
    }

    private Type parseType(){
        if(match(TokenType.INT)) return Type.INT;
        if(match(TokenType.CHAR)) return Type.CHAR;
        if(match(TokenType.VOID)) return Type.VOID;
        throw error("Expected type");
    }

    private Expression parseExpression(){
        return parseAssignment();
    }

    private Expression parseAssignment() {

        Expression expression = parseLogicalOr();

        if (match(TokenType.ASSIGN)) {

            Expression value = parseAssignment();

            if (expression instanceof VariableExpression variable) {
                return new AssignmentExpression(
                        variable.name(),
                        value
                );
            }

            throw error("Invalid assignment target.");
        }

        return expression;
    }

    private Expression parseLogicalOr() {

        Expression expression = parseLogicalAnd();

        while (match(TokenType.LOGICAL_OR)) {

            Token operator = previous();

            Expression right = parseLogicalAnd();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseLogicalAnd() {

        Expression expression = parseEquality();

        while (match(TokenType.LOGICAL_AND)) {

            Token operator = previous();

            Expression right = parseEquality();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseEquality() {

        Expression expression = parseComparison();

        while (match(TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL)) {

            Token operator = previous();

            Expression right = parseComparison();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseComparison() {

        Expression expression = parseTerm();

        while (match(
                TokenType.LESS,
                TokenType.LESS_EQUAL,
                TokenType.GREATER,
                TokenType.GREATER_EQUAL
        )) {

            Token operator = previous();

            Expression right = parseTerm();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseTerm() {

        Expression expression = parseFactor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {

            Token operator = previous();

            Expression right = parseFactor();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseFactor() {

        Expression expression = parseUnary();

        while (match(
                TokenType.STAR,
                TokenType.SLASH,
                TokenType.PERCENT
        )) {

            Token operator = previous();

            Expression right = parseUnary();

            expression = new BinaryExpression(
                    expression,
                    operator,
                    right
            );
        }

        return expression;
    }

    private Expression parseUnary() {

        if (match(
                TokenType.MINUS,
                TokenType.LOGICAL_NOT,
                TokenType.BIT_NOT
        )) {

            Token operator = previous();

            Expression operand = parseUnary();

            return new UnaryExpression(
                    operator,
                    operand
            );
        }

        return parsePrimary();
    }

    private Expression parsePrimary() {

        if (match(TokenType.INTEGER)) {
            return new LiteralExpression(
                    Integer.parseInt(previous().lexeme())
            );
        }

        if (match(TokenType.CHARACTER)) {
            return new LiteralExpression(
                    previous().lexeme()
            );
        }

        if (match(TokenType.STRING)) {
            return new LiteralExpression(
                    previous().lexeme()
            );
        }

        if (match(TokenType.IDENTIFIER)) {

            Token name = previous();

            // Function call
            if (match(TokenType.LEFT_PAREN)) {

                List<Expression> arguments = new ArrayList<>();

                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        arguments.add(parseExpression());
                    } while (match(TokenType.COMMA));
                }

                consume(
                        TokenType.RIGHT_PAREN,
                        "Expected ')' after arguments."
                );

                return new CallExpression(
                        name.lexeme(),
                        arguments
                );
            }

            // Variable
            return new VariableExpression(
                    name.lexeme()
            );
        }

        if (match(TokenType.LEFT_PAREN)) {

            Expression expression = parseExpression();

            consume(
                    TokenType.RIGHT_PAREN,
                    "Expected ')'."
            );

            return expression;
        }

        throw error("Expected expression.");
    }

    private BlockStatement parseBlock() {

        List<BlockItem> items = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {

            if (check(TokenType.INT)
                    || check(TokenType.CHAR)
                    || check(TokenType.VOID)) {

                items.add(parseDeclaration());

            } else {

                items.add(parseStatement());

            }
        }

        consume(
                TokenType.RIGHT_BRACE,
                "Expected '}' after block."
        );

        return new BlockStatement(items);
    }

    private Statement parseStatement() {

        if (match(TokenType.LEFT_BRACE)) {
            return parseBlock();
        }

        if (match(TokenType.IF)) {
            return parseIf();
        }

        if (match(TokenType.WHILE)) {
            return parseWhile();
        }

        if (match(TokenType.FOR)) {
            return parseFor();
        }

        if (match(TokenType.RETURN)) {
            return parseReturn();
        }

        Expression expression = parseExpression();

        consume(
                TokenType.SEMICOLON,
                "Expected ';' after expression."
        );

        return new ExpressionStatement(expression);
    }

    private ReturnStatement parseReturn() {

        if (match(TokenType.SEMICOLON)) {
            return new ReturnStatement(null);
        }

        Expression value = parseExpression();

        consume(
                TokenType.SEMICOLON,
                "Expected ';' after return value."
        );

        return new ReturnStatement(value);
    }

    private WhileStatement parseWhile() {

        consume(
                TokenType.LEFT_PAREN,
                "Expected '(' after while."
        );

        Expression condition = parseExpression();

        consume(
                TokenType.RIGHT_PAREN,
                "Expected ')' after condition."
        );

        Statement body = parseStatement();

        return new WhileStatement(
                condition,
                body
        );
    }

    private IfStatement parseIf() {

        consume(
                TokenType.LEFT_PAREN,
                "Expected '(' after if."
        );

        Expression condition = parseExpression();

        consume(
                TokenType.RIGHT_PAREN,
                "Expected ')' after condition."
        );

        Statement thenBranch = parseStatement();

        Statement elseBranch = null;

        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement();
        }

        return new IfStatement(
                condition,
                thenBranch,
                elseBranch
        );
    }

    private ForStatement parseFor() {

        consume(
                TokenType.LEFT_PAREN,
                "Expected '(' after for."
        );

        // Initializer
        Statement initializer = null;

        if (!check(TokenType.SEMICOLON)) {

            Expression init = parseExpression();

            initializer = new ExpressionStatement(init);
        }

        consume(
                TokenType.SEMICOLON,
                "Expected ';' after for initializer."
        );

        // Condition
        Expression condition = null;

        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression();
        }

        consume(
                TokenType.SEMICOLON,
                "Expected ';' after for condition."
        );

        // Increment
        Expression increment = null;

        if (!check(TokenType.RIGHT_PAREN)) {
            increment = parseExpression();
        }

        consume(
                TokenType.RIGHT_PAREN,
                "Expected ')' after for clauses."
        );

        Statement body = parseStatement();

        return new ForStatement(
                initializer,
                condition,
                increment,
                body
        );
    }

    private boolean match(TokenType...ts){ for(TokenType t:ts){ if(check(t)){ advance(); return true; } } return false; }
    private boolean check(TokenType t){ return !isAtEnd() && peek().type()==t; }
    private Token advance(){ if(!isAtEnd()) current++; return previous(); }
    private Token consume(TokenType t,String m){ if(check(t)) return advance(); throw error(m); }
    private Token peek(){ return Tokens.get(current); }
    private Token previous(){ return Tokens.get(current-1); }
    private boolean isAtEnd(){ return peek().type()==TokenType.EOF; }
    private RuntimeException error(String m){ return new RuntimeException(m+" at "+peek().line()+":"+peek().column()); }
}
