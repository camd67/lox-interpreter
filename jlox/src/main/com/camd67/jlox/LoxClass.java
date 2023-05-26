package com.camd67.jlox;

import java.util.List;
import java.util.Map;

/**
 * The abstract class in lox.
 * See also LoxInstance for the actual runtime representation.
 */
public class LoxClass implements LoxCallable {

    final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    @Override
    public int arity() {
        var initializer = findMethod("init");

        // No initializer? no arity.
        if (initializer == null) {
            return 0;
        } else {
            return initializer.arity();
        }
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var instance = new LoxInstance(this);

        // If we have an initializer, call it
        var initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        return null;
    }
}
