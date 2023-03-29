package com.camd67.jlox;

import java.util.List;

import static com.camd67.jlox.TokenType.*;

/**
 * Parser for the lox language.
 */
public class Parser {
    public static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Begins parsing the current list of tokens.
     * Returns null if an error occurred during parsing.
     */
    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    /**
     * Grammar rule:
     * expression -> equality
     */
    private Expr expression() {
        return equality();
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
            return primary();
        }
    }

    /**
     * Grammar rule:
     * primary -> NUMBER| STRING | "true" | "false" | "nil" | "(" expression ")"
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
        Lox.error(token, message);
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
