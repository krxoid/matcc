package com.krxoid.codegen;

import java.util.ArrayList;
import java.util.List;

public class AssemblyEmitter {

    private final List<Instruction> instructions = new ArrayList<>();

    public void emit(String opcode, String... operands) {
        instructions.add(new Instruction(opcode, operands));
    }

    public List<Instruction> instructions() {
        return instructions;
    }

    public String output() {
        StringBuilder builder = new StringBuilder();

        for (Instruction instruction : instructions) {
            builder.append(instruction)
                    .append('\n');
        }

        return builder.toString();
    }
}