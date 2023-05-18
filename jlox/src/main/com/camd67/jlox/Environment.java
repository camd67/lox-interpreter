package com.camd67.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the state (variables) for a given environment.
 */
public class Environment {
    /**
     * Placeholder for what we can put in the map to signal
     * "this variable is defined, but not assigned a value yet"
     * we can't use nil/null since that is a valid value to assign
     */
    private static final Object VARIABLE_NOT_INITIALIZED = new Object();

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
            var value = values.get(name.lexeme);

            if (value != VARIABLE_NOT_INITIALIZED) {
                return value;
            } else {
                throw new RuntimeError(name, "Variable not yet initialized '" + name.lexeme + "'.");
            }
        } else if (enclosing != null) {
            // If not, continue up the chain
            return enclosing.get(name);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

    /**
     * Defines a variable in our environment with no initializer
     */
    void define(String name) {
        values.put(name, VARIABLE_NOT_INITIALIZED);
    }

    /**
     * Defines a variable in our environment with a value
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

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Returns back the environment that is distance steps away from
     * the current environment.
     * Mostly used to resolve variables to a specific environment.
     */
    private Environment ancestor(int distance) {
        var environment = this;
        for (var i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }

    /**
     * Assigns the given name/value pair at the environment distance steps away.
     */
    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}
