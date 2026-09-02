package com.krxoid.ast;

import java.util.List;

public record Program(
        List<Declaration> declarations
) {}