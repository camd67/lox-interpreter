package com.camd67.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state (variables) for a given environment.
 */
public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Gets a variable from our environment.
     * Throws a runtime error if that variable is not defined yet.
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

    /**
     * Defines a variable in our environment
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }
}
