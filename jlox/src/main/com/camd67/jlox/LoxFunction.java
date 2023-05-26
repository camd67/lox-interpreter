package com.camd67.jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    /**
     * Track if this function is a class initializer.
     * We've got some special logic to do when it is.
     */
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var environment = new Environment(closure);
        for (var i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnStmt) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return returnStmt.value;
        }

        if (isInitializer) {
            // If we're an initializer we want to always return 'this'
            // to allow chaining after the constructor.
            // That "field" is always at the parent env.
            return closure.getAt(0, "this");
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    /**
     * Binds this function to a given lox instance
     */
    LoxFunction bind(LoxInstance loxInstance) {
        var environment = new Environment(closure);
        environment.define("this", loxInstance);
        return new LoxFunction(declaration, environment, isInitializer);
    }
}
