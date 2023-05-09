package com.camd67.jlox;

import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private enum State {
        /**
         * If we're in a loop during normal execution
         */
        IN_LOOP,

        /**
         * Track if we're trying to break out of a loop or not.
         * This seems like it would probably be better to just
         * jump to the end of the loop but we don't have access
         * to that right now.
         */
        BREAKING_LOOP,

        /**
         * Normal everyday execution
         */
        REGULAR_EXECUTION,
        ;
    }

    private final LoxGlobal lox;
    private State state = State.REGULAR_EXECUTION;

    /**
     * Global environment everyone has access to.
     */
    final Environment globals = new Environment();

    /**
     * The current environment the interpreter is interpreting.
     */
    private Environment environment = globals;

    public Interpreter(LoxGlobal lox) {
        this.lox = lox;

        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            lox.runtimeError(error);
        }
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (state != State.IN_LOOP) {
            throw new RuntimeError(stmt.token, "Break occurred outside loop");
        }
        state = State.BREAKING_LOOP;
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            // We only consider ourselves in the loop when we're actually
            // inside the loop body. Not in the condition.
            state = State.IN_LOOP;
            execute(stmt.body);
            if (state == State.BREAKING_LOOP) {
                state = State.REGULAR_EXECUTION;
                return null;
            }
            state = State.REGULAR_EXECUTION;
        }
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        var function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        var value = evaluate(stmt.expression);
        lox.logOut(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        // This could probably be implemented the same way as the state
        // tracking for break statements... but I'll follow the book
        // for this one.
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer != null) {
            var value = evaluate(stmt.initializer);
            environment.define(stmt.name.lexeme, value);
        } else {
            environment.define(stmt.name.lexeme);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = evaluate(expr.value);
        environment.assign(expr.type, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG_EQUAL -> {
                return !isEqual(left, right);
            }
            case EQUAL_EQUAL -> {
                return isEqual(left, right);
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(expr.operator, "Division by zero");
                }
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case PLUS -> {
                // If both are doubles we do math
                // Otherwise if either of the two are strings we stringify the other and concat
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                } else if (left instanceof String) {
                    return left + stringify(right);
                } else if (right instanceof String) {
                    return stringify(left) + right;
                }

                throw new RuntimeError(
                    expr.operator,
                    "Operands must be either be two numbers or one of the two operands a string."
                );
            }
        }

        // unreachable
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        var callee = evaluate(expr.callee);

        var args = new ArrayList<Object>();
        for (var argument : expr.arguments) {
            args.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        if (args.size() != function.arity()) {
            throw new RuntimeError(
                expr.paren,
                "Expected " + function.arity() + " arguments but got " + args.size() + "."
            );
        }
        return function.call(this, args);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS -> {
                return -(double) right;
            }
            case BANG -> {
                return !isTruthy(right);
            }
        }

        // unreachable
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTruthy(evaluate(expr.check))) {
            return evaluate(expr.left);
        } else {
            return evaluate(expr.right);
        }
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) {
        } else {
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
        } else {
            throw new RuntimeError(operator, "Operands must be a numbers.");
        }
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        } else if (left == null) {
            return false;
        } else {
            return left.equals(right);
        }
    }

    /**
     * Determine if our value is truthy or not.
     * Falsy values are null and `false`.
     * Everything else is true.
     */
    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return true;
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt statement) {
        if (state == State.BREAKING_LOOP) {
            return;
        }
        statement.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment newEnv) {
        // Store our current env so we can push it back after running all statements in this block
        var previousEnv = this.environment;
        try {
            this.environment = newEnv;
            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previousEnv;
        }
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        } else if (object instanceof Double) {
            var text = object.toString();
            // Trim off the .0 if present so it looks like an int
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        } else {
            return object.toString();
        }
    }
}
