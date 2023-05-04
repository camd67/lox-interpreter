package com.camd67.jlox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoxTest {
    private static Stream<Arguments> fileSources() {
        return Stream.of(
            Arguments.of(
                "expressions",
                """
                    -2.6666666666666665
                    0
                    -1
                    """
            ),
            Arguments.of(
                "blocks",
                """
                    inner a
                    outer b
                    global c

                    outer a
                    outer b
                    global c

                    global a
                    global b
                    global c
                    """
            ),
            Arguments.of(
                "scopedUseAndDefine",
                """
                    3
                    1
                    """
            ),
            Arguments.of(
                "if",
                """
                    expected1
                    expected2
                    expected3
                    expected4
                    expected7
                    """
            ),
            Arguments.of(
                "while",
                """
                    0
                    1
                    2
                    3
                    4
                    5
                    6
                    7
                    8
                    9
                    """
            ),
            Arguments.of(
                "for",
                """
                    0
                    1
                    2
                    reuse a
                    0
                    out of loop
                    0
                    partial for
                    0
                    1
                    2
                    """
            ),
            Arguments.of(
                "break",
                """
                    loop 2
                    0
                    after check
                    1
                    after check
                    2
                    loop 3
                    breaking!
                    """
            )
        );
    }

    @ParameterizedTest(name = "Test file - {0}.lox")
    @MethodSource("fileSources")
    void testFileSources(String loxFilename, String expectedOutput) throws IOException {
        try (var mockLox = new LoxTestUtil.TestLox()) {
            mockLox.lox.runFile("lox/" + loxFilename + ".lox");

            mockLox.assertNoErrOutput();
            mockLox.assertOutputEquals(expectedOutput);
        }
    }

    @Test
    void useBeforeDefine() throws IOException {
        IntConsumer expectedExit = (int i) -> assertEquals(70, i);
        try (var mockLox = new LoxTestUtil.TestLox(expectedExit)) {
            mockLox.lox.runFile("lox/useBeforeDefine.lox");

            mockLox.assertErrEquals(
                """
                    Variable not yet initialized 'c'.
                    [line 17]
                    """
            );
            mockLox.assertOutputEquals(
                """
                    assigned
                    nil
                    inside
                    """
            );
        }
    }
}

