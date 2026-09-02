package com.krxoid.ast;

import java.util.List;

public record BlockStatement(
        List<BlockItem> items
) implements Statement {
}