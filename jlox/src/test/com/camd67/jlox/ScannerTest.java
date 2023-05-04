package com.camd67.jlox;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.camd67.jlox.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ScannerTest {
    static Stream<Arguments> scanForTokens_source() {
        return Stream.of(
            // Basic parsing
            Arguments.of(
                "Basic",
                "(a+1)/2",
                List.of(
                    new Token(LEFT_PAREN, "(", null, 1),
                    new Token(IDENTIFIER, "a", null, 1),
                    new Token(PLUS, "+", null, 1),
                    new Token(NUMBER, "1", 1.0, 1),
                    new Token(RIGHT_PAREN, ")", null, 1),
                    new Token(SLASH, "/", null, 1),
                    new Token(NUMBER, "2", 2.0, 1),
                    new Token(EOF, "", null, 1)
                )
            ),
            Arguments.of(
                "All Tokens",
                "(){},.-+;*/?:! != == = > >= < <= abcde \"abcde\" 123 and class else " +
                    "false fun for if nil or print return super this true var while break",
                List.of(
                    new Token(LEFT_PAREN, "(", null, 1),
                    new Token(RIGHT_PAREN, ")", null, 1),
                    new Token(LEFT_BRACE, "{", null, 1),
                    new Token(RIGHT_BRACE, "}", null, 1),
                    new Token(COMMA, ",", null, 1),
                    new Token(DOT, ".", null, 1),
                    new Token(MINUS, "-", null, 1),
                    new Token(PLUS, "+", null, 1),
                    new Token(SEMICOLON, ";", null, 1),
                    new Token(STAR, "*", null, 1),
                    new Token(SLASH, "/", null, 1),
                    new Token(QUESTION, "?", null, 1),
                    new Token(COLON, ":", null, 1),
                    new Token(BANG, "!", null, 1),
                    new Token(BANG_EQUAL, "!=", null, 1),
                    new Token(EQUAL_EQUAL, "==", null, 1),
                    new Token(EQUAL, "=", null, 1),
                    new Token(GREATER, ">", null, 1),
                    new Token(GREATER_EQUAL, ">=", null, 1),
                    new Token(LESS, "<", null, 1),
                    new Token(LESS_EQUAL, "<=", null, 1),
                    new Token(IDENTIFIER, "abcde", null, 1),
                    new Token(STRING, "\"abcde\"", "abcde", 1),
                    new Token(NUMBER, "123", 123.0, 1),
                    new Token(AND, "and", null, 1),
                    new Token(CLASS, "class", null, 1),
                    new Token(ELSE, "else", null, 1),
                    new Token(FALSE, "false", null, 1),
                    new Token(FUN, "fun", null, 1),
                    new Token(FOR, "for", null, 1),
                    new Token(IF, "if", null, 1),
                    new Token(NIL, "nil", null, 1),
                    new Token(OR, "or", null, 1),
                    new Token(PRINT, "print", null, 1),
                    new Token(RETURN, "return", null, 1),
                    new Token(SUPER, "super", null, 1),
                    new Token(THIS, "this", null, 1),
                    new Token(TRUE, "true", null, 1),
                    new Token(VAR, "var", null, 1),
                    new Token(WHILE, "while", null, 1),
                    new Token(BREAK, "break", null, 1),
                    new Token(EOF, "", null, 1)
                )
            ),
            Arguments.of(
                "Line comment",
                "123;// None of this should be here! == 123",
                List.of(
                    new Token(NUMBER, "123", 123.0, 1),
                    new Token(SEMICOLON, ";", null, 1),
                    new Token(EOF, "", null, 1)
                )
            ),
            Arguments.of(
                "Block comment",
                "123/* ignore this! true */==",
                List.of(
                    new Token(NUMBER, "123", 123.0, 1),
                    new Token(EQUAL_EQUAL, "==", null, 1),
                    new Token(EOF, "", null, 1)
                )
            ),
            Arguments.of(
                "Line number tracking",
                """
                    123
                    true
                    false""",
                List.of(
                    new Token(NUMBER, "123", 123.0, 1),
                    new Token(TRUE, "true", null, 2),
                    new Token(FALSE, "false", null, 3),
                    new Token(EOF, "", null, 3)
                )
            ),
            Arguments.of(
                "whitespace doesn't matter",
                "(a\t+                  1)          / \t\t\t2",
                List.of(
                    new Token(LEFT_PAREN, "(", null, 1),
                    new Token(IDENTIFIER, "a", null, 1),
                    new Token(PLUS, "+", null, 1),
                    new Token(NUMBER, "1", 1.0, 1),
                    new Token(RIGHT_PAREN, ")", null, 1),
                    new Token(SLASH, "/", null, 1),
                    new Token(NUMBER, "2", 2.0, 1),
                    new Token(EOF, "", null, 1)
                )
            ),
            Arguments.of(
                "Identifiers",
                "alpha123 alpha _123 _ _a_ ___",
                List.of(
                    new Token(IDENTIFIER, "alpha123", null, 1),
                    new Token(IDENTIFIER, "alpha", null, 1),
                    new Token(IDENTIFIER, "_123", null, 1),
                    new Token(IDENTIFIER, "_", null, 1),
                    new Token(IDENTIFIER, "_a_", null, 1),
                    new Token(IDENTIFIER, "___", null, 1),
                    new Token(EOF, "", null, 1)
                )
            )
        );
    }

    @ParameterizedTest(name = "Scan for tokens - {0}")
    @MethodSource("scanForTokens_source")
    void scansForTokens(String name, String input, List<Token> output) {
        assertEquals(output, new Scanner(input, LoxTestUtil.realLox()).scanTokens());
    }
}