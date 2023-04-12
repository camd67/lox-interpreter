package com.camd67.jlox;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final LoxGlobal lox;
    private Environment environment = new Environment();

    public Interpreter(LoxGlobal lox) {
        this.lox = lox;
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
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
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
                checkNumberOperand(expr.operator, right);
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
            return;
        } else {
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
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
        statement.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment newEnv) {
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
