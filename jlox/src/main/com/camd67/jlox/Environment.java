package com.camd67.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state (variables) for a given environment.
 */
public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    /**
     * The enclosing environment that parent's this environment.
     * Nullable if we're at the root environment.
     */
    final Environment enclosing;

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Gets a variable from our environment or any of the enclosing environments.
     * Throws a runtime error if that variable is not defined yet.
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            // First look in our env for the token
            return values.get(name.lexeme);
        } else if (enclosing != null) {
            // If not, continue up the chain
            return enclosing.get(name);
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

    /**
     * Assigns a value to a given token, in this environment
     * or the closest enclosing environment.
     */
    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }
}
