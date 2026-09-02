package com.krxoid.semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class SymbolTable {

    private final Deque<Map<String, Symbol>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        pushScope(); // Global scope
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {

        if (scopes.size() == 1) {
            throw new IllegalStateException("Cannot remove global scope.");
        }

        scopes.pop();
    }

    public void define(Symbol symbol) {

        Map<String, Symbol> current = scopes.peek();

        if (current.containsKey(symbol.name())) {
            throw new IllegalStateException(
                    "Duplicate symbol: " + symbol.name()
            );
        }

        current.put(symbol.name(), symbol);
    }

    public Symbol lookup(String name) {

        for (Map<String, Symbol> scope : scopes) {

            Symbol symbol = scope.get(name);

            if (symbol != null) {
                return symbol;
            }
        }

        return null;
    }

    public boolean isDefined(String name) {
        return lookup(name) != null;
    }

    public boolean isDefinedInCurrentScope(String name) {

        Map<String, Symbol> current = scopes.peek();

        return current.containsKey(name);
    }

    public int scopeDepth() {
        return scopes.size();
    }

    public void clear() {
        scopes.clear();
        pushScope();
    }
}