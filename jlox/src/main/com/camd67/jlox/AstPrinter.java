package com.camd67.jlox;

import java.util.List;

/**
 * NOTE:
 * This class currently has a lot of places where things are in an in-progress state
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    void print(List<Stmt> statements, LoxGlobal lox) {
        for (var statement : statements) {
            lox.logOut(statement.accept(this));
        }
    }

    private String parenthesize(String name, Expr... exprs) {
        var builder = new StringBuilder();
        builder.append("(").append(name);
        for (var expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize("?", expr.check, expr.left, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("= " + expr.type.lexeme, expr.value);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize("call", expr.callee);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitBreakStmt(Stmt.Break stmt) {
        return "(break)";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "WHILE TODO";
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        return "IF TODO";
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        return "FUN TODO";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return "BLOCK TODO";
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.accept(this);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return parenthesize("return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return parenthesize("var " + stmt.name, stmt.initializer);
    }
}
