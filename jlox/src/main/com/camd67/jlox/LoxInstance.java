package com.camd67.jlox;

import java.util.HashMap;
import java.util.Map;

/**
 * A runtime instance of a lox class, created by calling the class
 * constructor.
 */
public class LoxInstance {
    /**
     * The current state of the fields in the instance.
     */
    private final Map<String, Object> fields = new HashMap<>();
    private final LoxClass klass;

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        // Fields get resolved first before methods
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        var method = klass.findMethod(name.lexeme);
        if (method != null) {
            return method.bind(this);
        }

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    @Override
    public String toString() {
        return "<instance " + klass.name + ">";
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
