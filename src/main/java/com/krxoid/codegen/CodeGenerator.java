package com.krxoid.codegen;

import com.krxoid.ast.*;
import com.krxoid.lexer.TokenType;

public final class CodeGenerator {

    private FrameLayout currentFrame;
    private final AssemblyEmitter emitter = new AssemblyEmitter();
    private final LabelGenerator labelGenerator = new LabelGenerator();

    public String generate(Program program) {

        for (Declaration declaration : program.declarations()) {

            if (declaration instanceof FunctionDeclaration function) {
                generateFunction(function);
            }
        }

        return "LDI r15 239 \n" + "CAL .main \n"+ "HLT \n" + emitter.output();
    }

    private void generateFunction(FunctionDeclaration function) {

        emitter.emit("." + function.name());

        FrameLayout frame = new FrameLayout(function);

        // Allocate every local
        collectLocals(function.body(), frame);

        // Reserve space for locals
        if (frame.localCount() > 0) {
            emitter.emit("ADI", "r15", Integer.toString(-frame.localCount()));
        }

        for (int i = 0; i < function.parameters().size(); i++) {

            emitter.emit(
                    "STR",
                    "r" + (i + 1),
                    "r15",
                    Integer.toString(frame.offsetOf(function.parameters().get(i).name()))
            );
        }

        // Make the frame available to expression/declaration generation
        currentFrame = frame;

        // Second pass: generate code
        for (BlockItem item : function.body().items()) {

            if (item instanceof Declaration declaration) {
                generateDeclaration(declaration);
            }
            else if (item instanceof Statement statement) {
                generateStatement(statement);
            }
        }

        // Restore frame pointer
        currentFrame = null;

    }

    private void generateFor(ForStatement stmt) {

        String start = labelGenerator.next("for_start");
        String end   = labelGenerator.next("for_end");

        // initializer
        if (stmt.initializer() != null) {
            generateStatement(stmt.initializer());
        }

        emitter.emit(start);

        // condition
        if (stmt.condition() != null) {
            generateExpression(stmt.condition());

            emitter.emit("CMP", "r1", "r0");
            emitter.emit("BRH", "EQ", end);
        }

        // body
        generateStatement(stmt.body());

        // increment
        if (stmt.increment() != null) {
            generateExpression(stmt.increment());
        }

        emitter.emit("JMP", start);

        emitter.emit(end);
    }

    private void generateStatement(Statement statement) {

        if (statement instanceof ReturnStatement ret) {

            generateExpression(ret.value());

            if (currentFrame.localCount() > 0) {
                emitter.emit(
                        "ADI",
                        "r15",
                        Integer.toString(currentFrame.localCount())
                );
            }

            emitter.emit("RET");
            return;
        }

        if (statement instanceof IfStatement ifStatement) {
            generateIf(ifStatement);
            return;
        }

        if (statement instanceof WhileStatement whileStatement) {
            generateWhile(whileStatement);
            return;
        }

        if (statement instanceof  ForStatement forStatement) {
            generateFor(forStatement);
            return;
        }


        if (statement instanceof BlockStatement block) {

            for (BlockItem item : block.items()) {

                if (item instanceof Declaration declaration) {
                    generateDeclaration(declaration);
                    return;
                }
                else if (item instanceof Statement stmt) {
                    generateStatement(stmt);
                }
            }

            return;
        }

        if (statement instanceof ExpressionStatement expr) {
            generateExpression(expr.expression());
            return;
        }

        throw new UnsupportedOperationException(
                "Unsupported statement: " + statement.getClass().getSimpleName()
        );
    }

    private void generateWhile(WhileStatement stmt) {

        String start = labelGenerator.next("while_start");
        String end   = labelGenerator.next("while_end");

        emitter.emit(start);

        generateExpression(stmt.condition());

        emitter.emit("CMP","r1","r0");
        emitter.emit("BRH","EQ",end);

        generateStatement(stmt.body());

        emitter.emit("JMP",start);

        emitter.emit(end);
    }

    private void collectLocals(BlockStatement block, FrameLayout frame) {

        for (BlockItem item : block.items()) {

            if (item instanceof VariableDeclaration variable) {
                frame.addLocal(variable);
            }

            if (item instanceof BlockStatement nested) {
                collectLocals(nested, frame);
            }
        }
    }

    private void generateExpression(Expression expression) {

        if (expression instanceof AssignmentExpression assignment) {

            generateExpression(assignment.value());

            emitter.emit(
                    "STR",
                    "r1",
                    "r15",
                    Integer.toString(currentFrame.offsetOf(assignment.name()))
            );

            return;
        }

        if (expression instanceof VariableExpression variable) {

            emitter.emit(
                    "LOD",
                    "r1",
                    "r15",
                    Integer.toString(currentFrame.offsetOf(variable.name()))
            );

            return;
        }

        if (expression instanceof LiteralExpression literal) {

            emitter.emit(
                    "LDI",
                    "r1",
                    literal.value().toString()
            );

            return;
        }

        if (expression instanceof UnaryExpression unary) {

            generateExpression(unary.operand());

            switch (unary.operator().type()) {

                case MINUS ->
                        emitter.emit("NEG", "r1");

                case LOGICAL_NOT ->
                        emitter.emit("NOT", "r1");

                default ->
                        throw new UnsupportedOperationException(
                                unary.operator().type().toString()
                        );
            }

            return;
        }

        if (expression instanceof BinaryExpression binary) {

            generateBinary(binary);
            return;
        }


        if (expression instanceof CallExpression call) {
            generateCall(call);
            return;
        }

        throw new UnsupportedOperationException(
                expression.getClass().getSimpleName()
        );
    }

    private void generateIf(IfStatement stmt) {

        String elseLabel = labelGenerator.next("if_else");
        String endLabel  = labelGenerator.next("if_end");

        generateExpression(stmt.condition());

        emitter.emit("CMP", "r1", "r0");
        emitter.emit("BRH", "EQ", elseLabel);

        // then
        generateStatement(stmt.thenBranch());

        emitter.emit("JMP", endLabel);

        // else
        emitter.emit(elseLabel);

        if (stmt.elseBranch() != null) {
            generateStatement(stmt.elseBranch());
        }

        emitter.emit(endLabel);
    }

    private void generateCall(CallExpression call) {

        for (int i = call.arguments().size() - 1; i >= 0; i--) {

            generateExpression(call.arguments().get(i));

            if (i != 0) {
                emitter.emit("MOV", "r1", "r" + (i + 1));
            }
        }

        emitter.emit("CAL", "." + call.functionName());
    }

    private void generateDeclaration(Declaration declaration) {

        if (declaration instanceof VariableDeclaration variable) {

            if (variable.initializer() != null) {

                generateExpression(variable.initializer());

                emitter.emit(
                        "STR",
                        "r1",
                        "r15",
                        Integer.toString(currentFrame.offsetOf(variable.name()))
                );
            }

            return;
        }

        throw new UnsupportedOperationException(
                declaration.getClass().getSimpleName()
        );
    }

    private void generateComparison(String condition,
                                    BinaryExpression binary) {

        generateExpression(binary.left());

        emitter.emit("MOV", "r1", "r2");

        generateExpression(binary.right());

        emitter.emit("CMP", "r2", "r1");

        String trueLabel = labelGenerator.next("cmp_true");
        String endLabel  = labelGenerator.next("cmp_end");

        emitter.emit("BRH", condition, trueLabel);

        emitter.emit("LDI", "r1", "0");
        emitter.emit("JMP", endLabel);

        emitter.emit(trueLabel);

        emitter.emit("LDI", "r1", "1");

        emitter.emit(endLabel);
    }

    private void generateBinary(BinaryExpression binary) {

        switch (binary.operator().type()) {

            case PLUS -> {
                generateExpression(binary.left());
                emitter.emit("MOV", "r1", "r2");
                generateExpression(binary.right());

                emitter.emit("ADD", "r2", "r1", "r1");
            }

            case MINUS -> {
                generateExpression(binary.left());
                emitter.emit("MOV", "r1", "r2");
                generateExpression(binary.right());

                emitter.emit("SUB", "r2", "r1", "r1");
            }

            case STAR ->
                    throw new UnsupportedOperationException(
                            "BatPU-2 has no MUL instruction yet."
                    );

            case SLASH ->
                    throw new UnsupportedOperationException(
                            "BatPU-2 has no DIV instruction."
                    );

            case PERCENT ->
                    throw new UnsupportedOperationException(
                            "Modulo not implemented."
                    );

            case LESS ->
                    generateComparison("LT", binary);

            case LESS_EQUAL ->
                    generateComparison("GE", binary);

            case GREATER ->
                    generateComparison("LT", binary);

            case GREATER_EQUAL ->
                    generateComparison("GE", binary);

            case EQUAL_EQUAL ->
                    generateComparison("EQ", binary);

            case NOT_EQUAL ->
                    generateComparison("NE", binary);

            default ->
                    throw new UnsupportedOperationException(
                            binary.operator().type().toString()
                    );
        }
    }
}