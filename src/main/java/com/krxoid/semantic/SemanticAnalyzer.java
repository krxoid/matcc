package com.krxoid.semantic;

import com.krxoid.ast.*;

public final class SemanticAnalyzer {

    private final SymbolTable symbols = new SymbolTable();
    private Type currentReturnType = null;

    public void analyze(Program program) {
        symbols.clear();

        // Register globals/functions first
        for (Declaration d : program.declarations()) {

            if (d instanceof FunctionDeclaration f) {
                symbols.define(
                        new Symbol(
                                f.name(),
                                f.returnType(),
                                Symbol.Kind.FUNCTION,
                                f
                        )
                );

            } else if (d instanceof VariableDeclaration v) {
                symbols.define(
                        new Symbol(
                                v.name(),
                                v.type(),
                                Symbol.Kind.VARIABLE,
                                null
                        )
                );
            }
        }

        // Analyze declarations...
    }

    private void analyzeDeclaration(Declaration d) {
        if (d instanceof FunctionDeclaration f) {
            analyzeFunction(f);
        } else if (d instanceof VariableDeclaration v) {
            analyzeVariable(v);
        } else {
            throw new IllegalStateException("Unknown declaration: " + d.getClass().getSimpleName());
        }
    }

    private void analyzeFunction(FunctionDeclaration fn) {
        currentReturnType = fn.returnType();
        symbols.pushScope();

        for (var p : fn.parameters()) {
            symbols.define(new Symbol(p.name(), p.type(), Symbol.Kind.PARAMETER, fn));
        }

        analyzeStatement(fn.body());

        symbols.popScope();
        currentReturnType = null;
    }

    private void analyzeVariable(VariableDeclaration v) {
        if (symbols.isDefinedInCurrentScope(v.name())) {
            throw new IllegalStateException("Duplicate variable: " + v.name());
        }

        symbols.define(new Symbol(v.name(), v.type(), Symbol.Kind.VARIABLE, null));

        if (v.initializer() != null) {
            analyzeExpression(v.initializer());
        }
    }
    private void analyzeFor(ForStatement statement) {

        if (statement.initializer() != null) {
            analyzeStatement(statement.initializer());
        }

        if (statement.condition() != null) {
            analyzeExpression(statement.condition());
        }

        if (statement.increment() != null) {
            analyzeExpression(statement.increment());
        }

        analyzeStatement(statement.body());
    }


    private void analyzeStatement(Statement s) {

        if (s instanceof BlockStatement b) {
            symbols.pushScope();
            for (BlockItem item : b.items()) {
                if (item instanceof Declaration d)
                    analyzeDeclaration(d);
                else
                    analyzeStatement((Statement)item);
            }
            symbols.popScope();
            return;
        }

        if (s instanceof ExpressionStatement e) {
            analyzeExpression(e.expression());
            return;
        }

        if (s instanceof IfStatement i) {
            analyzeExpression(i.condition());
            analyzeStatement(i.thenBranch());
            if (i.elseBranch() != null)
                analyzeStatement(i.elseBranch());
            return;
        }

        if (s instanceof WhileStatement w) {
            analyzeExpression(w.condition());
            analyzeStatement(w.body());
            return;
        }

        if (s instanceof ForStatement forStatement) {
            analyzeFor(forStatement);
            return;
        }

        if (s instanceof ReturnStatement r) {
            if (r.value() != null)
                analyzeExpression(r.value());
            return;
        }

        throw new IllegalStateException("Unknown statement: " + s.getClass().getSimpleName());
    }

    private void analyzeExpression(Expression e) {

        if (e instanceof LiteralExpression)
            return;

        if (e instanceof VariableExpression v) {
            if (!symbols.isDefined(v.name()))
                throw new IllegalStateException("Undefined variable: " + v.name());
            return;
        }

        if (e instanceof AssignmentExpression a) {
            if (!symbols.isDefined(a.name()))
                throw new IllegalStateException("Undefined variable: " + a.name());
            analyzeExpression(a.value());
            return;
        }

        if (e instanceof UnaryExpression u) {
            analyzeExpression(u.operand());
            return;
        }

        if (e instanceof BinaryExpression b) {
            analyzeExpression(b.left());
            analyzeExpression(b.right());
            return;
        }

        if (e instanceof CallExpression c) {
            Symbol fn = symbols.lookup(c.functionName());
            if (fn == null || fn.kind() != Symbol.Kind.FUNCTION)
                throw new IllegalStateException("Undefined function: " + c.functionName());

            for (Expression arg : c.arguments())
                analyzeExpression(arg);

            return;
        }

        throw new IllegalStateException("Unknown expression: " + e.getClass().getSimpleName());
    }
}
