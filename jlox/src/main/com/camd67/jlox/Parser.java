package com.camd67.jlox;

import java.util.ArrayList;
import java.util.List;

import static com.camd67.jlox.TokenType.*;

/**
 * Parser for the lox language.
 */
public class Parser {
    public static class ParseError extends RuntimeException {
    }

    private final LoxGlobal lox;
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens, LoxGlobal lox) {
        this.tokens = tokens;
        this.lox = lox;
    }

    /**
     * Begins parsing the current list of tokens.
     * Grammar rule:
     * program -> declaration* EOF
     */
    List<Stmt> parse() {
        var statements = new ArrayList<Stmt>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    /**
     * Grammar rule:
     * declaration -> varDecl | statement;
     */
    private Stmt declaration() {
        try {
            if (match(FUN)) {
                return function("function");
            } else if (match(VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    /**
     * Grammar rule:
     * funDecl -> "fun" function;
     * function -> IDENTIFIER "(" parameters ? ")" block;
     * parameters -> IDENTIFIER ("<" IDENTIFIER)*;
     */
    private Stmt function(String kind) {
        var name = consume(IDENTIFIER, "Expect " + kind + " name.");

        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");
        var parameters = new ArrayList<Token>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    // Don't throw since we're in a known state
                    error(peek(), "Can't have more than 255 parameters");
                }
                parameters.add(consume(IDENTIFIER, "Expected parameter name."));
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        // Consume the left brace so the block function is in the correct state
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        var body = block();
        return new Stmt.Function(name, parameters, body);
    }

    /**
     * Grammar rule:
     * varDecl -> "var" IDENTIFIER ("=" expression)? ";"
     */
    private Stmt varDeclaration() {
        var name = consume(IDENTIFIER, "Expect variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    /**
     * Grammar rule:
     * statement -> expressionStmt | ifStmt | printStmt | returnStmt | whileStmt | break | block
     */
    private Stmt statement() {
        if (match(PRINT)) {
            return printStatement();
        } else if (match(IF)) {
            return ifStatement();
        } else if (match(LEFT_BRACE)) {
            return new Stmt.Block(block());
        } else if (match(RETURN)) {
            return returnStatement();
        } else if (match(WHILE)) {
            return whileStatement();
        } else if (match(FOR)) {
            return forStatement();
        } else if (match(BREAK)) {
            return breakStatement();
        } else {
            return expressionStatement();
        }
    }

    private Stmt returnStatement() {
        var keyword = previous();
        // Assume we return null
        Expr value = null;
        // But if there's something there...
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    /**
     * Grammar rule:
     * breakStmt -> "break" ";"
     */
    private Stmt.Break breakStatement() {
        var breakToken = previous();
        consume(SEMICOLON, "Expect ';' after 'break'.");
        return new Stmt.Break(breakToken);
    }

    /**
     * Note this is a sugared expression, thus this will break down into
     * other statements as opposed to a for statement.
     * Grammar rule:
     * forStmt -> "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement ;
     */
    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        var body = statement();

        // Desugar our for loop into
        // {
        //   initializer
        //   while (condition) {
        //     body
        //     increment
        //   }
        // }

        // Do we have an increment?
        // Stick it at the end of the body
        if (increment != null) {
            body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
        }

        // Missing condition? It's always while(true) then
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);

        // Do we have an initializer? Place that in a block above the existing body.
        // This must be in it's own block so we don't corrupt any outer scopes.
        if (initializer != null) {
            body = new Stmt.Block(List.of(initializer, body));
        }

        return body;
    }

    /**
     * Grammar rule:
     * whileStmt -> "while" "(" expression ")" statement;
     */
    private Stmt.While whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        var body = statement();
        return new Stmt.While(condition, body);
    }

    /**
     * Grammar rule:
     * ifStmt -> "if" "(" expression ")" statement ( "else" statement )? ;
     */
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        var thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * Grammar rule:
     * block -> "{" declaration* "}"
     */
    private List<Stmt> block() {
        var statements = new ArrayList<Stmt>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block");
        return statements;
    }

    /**
     * Grammar rule:
     * printStmt -> "print" expression ";"
     */
    private Stmt printStatement() {
        var value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    /**
     * Grammar rule:
     * exprStmt -> expression ";"
     */
    private Stmt expressionStatement() {
        var value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(value);
    }

    /**
     * Grammar rule:
     * expression -> assignment;
     */
    private Expr expression() {
        return assignment();
    }

    /**
     * Grammar rule:
     * assignment -> (IDENTIFIER "=" assignment)? ternary;
     */
    private Expr assignment() {
        // Since we can't do a lookahead we'll just parse out the left hand side of the (possible) assignment.
        // If we get back from that and find it is followed by an equals then we must've hit an assignment.
        // We'll keep parsing and report an error if our left side isn't a variable/identifier
        var expr = ternary();
        if (match(EQUAL)) {
            var equals = previous();
            var value = ternary();
            if (expr instanceof Expr.Variable) {
                var name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            // Don't throw our error since we don't need to synchronize after this.
            // We can safely keep parsing even though we hit an error
            error(equals, "Invalid assignment target");
        }
        return expr;
    }

    /**
     * Grammar rule:
     * ternary -> equality ( "?" ternary ":" ternary )
     */
    private Expr ternary() {
        var expr = equality();
        if (match(QUESTION)) {
            var left = ternary();
            consume(COLON, "mismatched ternary colon");
            var right = ternary();
            expr = new Expr.Ternary(expr, left, right);
        }
        return expr;
    }

    /**
     * Grammar rule:
     * equality -> comparison (("!=" | "==") comparison)*
     */
    private Expr equality() {
        var expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            var operator = previous();
            var right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Grammar rule:
     * comparison -> term (( ">" | ">=" | "<" | "<=") term)*
     */
    private Expr comparison() {
        var expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previous();
            var right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Grammar rule:
     * term -> factor ( ("-" | "+" ) factor )*
     */
    private Expr term() {
        var expr = factor();
        while (match(MINUS, PLUS)) {
            var operator = previous();
            var right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Grammar rule:
     * factor -> unary ( ("/" | "*") unary)*
     */
    private Expr factor() {
        var expr = unary();
        while (match(SLASH, STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Grammar rule:
     * unary -> ("!" | "-") unary | primary;
     */
    private Expr unary() {
        if (match(BANG, MINUS)) {
            var operator = previous();
            var right = unary();
            return new Expr.Unary(operator, right);
        } else {
            return call();
        }
    }

    /**
     * Grammar rule:
     * call -> primary ( "(" arguments? ")" )*;
     */
    private Expr call() {
        var expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    /**
     * "Grammar rule":
     * arguments -> expression ( "," expression )*;
     */
    private Expr finishCall(Expr callee) {
        var arguments = new ArrayList<Expr>();
        // If we don't have a right paren yet
        if (!check(RIGHT_PAREN)) {
            // Start consuming arguments
            do {
                if (arguments.size() >= 255) {
                    // don't throw this since we're in a known "good" state
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        var paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    /**
     * Grammar rule:
     * primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
     */
    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        } else if (match(TRUE)) {
            return new Expr.Literal(true);
        } else if (match(NIL)) {
            return new Expr.Literal(null);
        } else if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        } else if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        } else if (match(LEFT_PAREN)) {
            var expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        } else {
            throw error(peek(), message);
        }
    }

    private ParseError error(Token token, String message) {
        lox.error(token, message);
        return new ParseError();
    }

    /**
     * If we enter into an error state, this will attempt to move to a known good spot we
     * can resume parsing from.
     */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            // semicolons terminate a statement, that means we've gotten to a new parsable
            if (previous().type == SEMICOLON) {
                return;
            }

            // Anything else we look at the next token and if it looks like a statement
            // then we'll start parsing there again
            switch (peek().type) {
                case CLASS, FOR, FUN, IF, PRINT, RETURN, VAR, WHILE -> {
                    return;
                }
                default -> {
                }
            }

            advance();
        }
    }

    /**
     * Matches any one of the provided token types.
     * If it matches then this function will both advance the parser
     * and also return true. False otherwise.
     */
    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks, but does not advance, that the next token is the provided type.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        } else {
            return peek().type == type;
        }
    }


    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Returns what token would be processed next
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Returns what token was just processed
     */
    private Token previous() {
        return tokens.get(current - 1);
    }
}
