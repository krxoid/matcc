package com.krxoid.codegen;

public record Instruction(
        String opcode,
        String[] operands
) {

    @Override
    public String toString() {
        if (operands.length == 0) {
            return opcode;
        }

        return opcode + " " + String.join(", ", operands);
    }
}
