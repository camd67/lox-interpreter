package com.camd67.jlox;

import java.util.ArrayList;
import java.util.List;

import static com.camd67.jlox.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We're not at the beginning of the next lexeme
            start = current;
            scanToken();
        }

        // Always add in our EOF at, well, the end of file
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        var c = advance();
        switch (c) {
            // Deal with all our single character values. These happen without question
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON); break;
            // Then deal with operators that are maybe 2 characters
            // These all peek ahead one character in case there's a combo
            case '!':
                if (match('=')) {
                    addToken(BANG_EQUAL);
                } else {
                    addToken(BANG);
                }
                break;
            case '=':
                if (match('=')) {
                    addToken(EQUAL_EQUAL);
                } else {
                    addToken(EQUAL);
                }
                break;
            case '<':
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else {
                    addToken(LESS);
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(GREATER_EQUAL);
                } else {
                    addToken(GREATER);
                }
                break;

            // Comments
            case '/':
                if (match('/')) {
                    // Double-comments consume till the end
                    while(peek() != '\n' && !isAtEnd()) {
                        advance();
                    }
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(SLASH);
                }
                break;

            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                // Some characters we just plain ignore
                break;
            case '\n':
                line++;
                break;

            case '"': string(); break;
            default:
                // we gotta deal with some stuff in default since they could just be anything
                if (isDigit(c)) {
                    // Numeric literals
                    number();
                } else if (isAlpha(c)) {
                    // Reserved words and identifiers
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private void identifier() {
        // Keep consuming all the numbers/letters
        while (isAlphaNumeric(peek())) {
            advance();
        }

        var text = source.substring(start, current);
        var type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }

    private void blockComment() {
        // Keep going until we hit a */ pair
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }
        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment");
            return;
        }

        // This should consume our *
        advance();
        // This should consume our /
        advance();
        // No tokens needed here, comments are ignored!
    }

    private void number() {
        // Keep consuming numbers
        while (isDigit(peek())) {
            advance();
        }

        // Check if we reached a . and there are numbers after this
        if (peek() == '.' && isDigit(peekNext())) {
            // consume the .
            advance();
            // then keep consuming the remaining numbers
            while (isDigit(peek())) {
                advance();
            }
        }

        var value = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, value);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // If we're in a multi-line string we gotta remember to bump line count
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        // We got to the end but must not have found a closing quote
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        // This should be our closing "
        advance();

        // Get the value of the string, but cut off the first and last "
        var value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Returns what would be next in the input without consuming it
     */
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        } else {
            return source.charAt(current);
        }
    }

    /**
     * Returns what would be 2 peeks ahead.
     * In essence, peek().peek()
     */
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        } else {
            return source.charAt(current + 1);
        }
    }

    /**
     * Conditionally advance if we see the expected character
     */
    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        } else if (source.charAt(current) != expected) {
            return false;
        } else {
            current++;
            return true;
        }
    }

    /**
     * Advance to the next character, returning that value
     */
    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
