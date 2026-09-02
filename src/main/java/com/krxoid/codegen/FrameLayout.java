package com.krxoid.codegen;

import com.krxoid.ast.FunctionDeclaration;
import com.krxoid.ast.VariableDeclaration;

import java.util.HashMap;
import java.util.Map;

public final class FrameLayout {

    private final FunctionDeclaration function;

    /*
     * Variable -> frame offset
     *
     * Example:
     *
     *  FP+0  parameter0
     *  FP+1  parameter1
     *  FP-1  local0
     *  FP-2  local1
     */
    private final Map<String, Integer> offsets = new HashMap<>();

    private int nextParameterOffset = 0;
    private int nextLocalOffset = -1;

    public FrameLayout(FunctionDeclaration function) {
        this.function = function;

        layoutParameters();
    }

    private void layoutParameters() {

        for (VariableDeclaration parameter : function.parameters()) {
            offsets.put(parameter.name(), nextParameterOffset++);
        }
    }

    public void addLocal(VariableDeclaration variable) {

        if (offsets.containsKey(variable.name())) {
            throw new IllegalStateException(
                    "Duplicate frame variable: " + variable.name()
            );
        }

        offsets.put(variable.name(), nextLocalOffset--);
    }

    public boolean contains(String name) {
        return offsets.containsKey(name);
    }

    public int offsetOf(String name) {

        Integer offset = offsets.get(name);

        if (offset == null) {
            throw new IllegalStateException(
                    "Unknown variable: " + name
            );
        }

        return offset;
    }

    public int localCount() {
        return -nextLocalOffset - 1;
    }

    public int parameterCount() {
        return nextParameterOffset;
    }

    public FunctionDeclaration function() {
        return function;
    }
}