package com.camd67.jlox;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoxBlockTest {

    @Test
    void nestedBlockFileWorks() throws IOException {
        try (var mockLox = new LoxTestUtil.TestLox()) {
            mockLox.lox.runFile("lox/blocks.lox");

            mockLox.assertNoErrOutput();
            var expectedOutput = LoxTestUtil.loxOutput(
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
                     """);
            assertEquals(expectedOutput, mockLox.loxOutStream.toString());
        }
    }

    @Test
    void scopedUseAndDefineWorks() throws IOException {
        try (var mockLox = new LoxTestUtil.TestLox()) {
            mockLox.lox.runFile("lox/scopedUseAndDefine.lox");

            mockLox.assertNoErrOutput();
            var expectedOutput = LoxTestUtil.loxOutput("""
                3
                1
                """);
            assertEquals(expectedOutput, mockLox.loxOutStream.toString());
        }
    }
}
