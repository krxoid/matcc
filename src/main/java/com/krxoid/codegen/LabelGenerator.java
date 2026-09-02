package com.krxoid.codegen;

public final class LabelGenerator {

    private int counter = 0;

    public String next(String prefix) {
        return "." + prefix + "_" + counter++;
    }

    public void reset() {
        counter = 0;
    }
}