package com.camd67.jlox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoxTestUtil {
    /**
     * Returns a new lox instance that is effectively what you would get
     * if you ran lox for reals.
     */
    public static Lox realLox() {
        return new Lox(System.in, System.out, System.err, System::exit);
    }

    /**
     * An instance of lox that sends all output and reads all input from test
     * local variables.
     * Read those variables for assertions.
     */
    public static class TestLox implements AutoCloseable {

        private static final IntConsumer defaultOnExit = (int i) ->
            assertEquals(0, i, "Exit called with a non-zero code");

        private final OutputStream loxOutStream;
        private final OutputStream loxErrStream;
        private final InputStream inputStream;
        public final IntConsumer onExit;
        public final Lox lox;

        public TestLox() {
            this("", defaultOnExit);
        }

        public TestLox(String inputData) {
            this(inputData, defaultOnExit);
        }

        public TestLox(IntConsumer onExit) {
            this("", onExit);
        }

        public TestLox(String inputData, IntConsumer onExit) {
            inputStream = new ByteArrayInputStream(inputData.getBytes(StandardCharsets.UTF_8));
            this.onExit = onExit;
            loxErrStream = new ByteArrayOutputStream();
            loxOutStream = new ByteArrayOutputStream();
            lox = new Lox(inputStream, new PrintStream(loxOutStream), new PrintStream(loxErrStream), onExit);
        }

        public void assertNoErrOutput() {
            assertEquals("", loxErrStream.toString());
        }

        public void assertOutputEquals(String expected) {
            assertEquals(expected, getOutput());
        }

        public void assertErrEquals(String expected) {
            assertEquals(expected, loxErrStream.toString().replaceAll("\r\n", "\n"));
        }

        @Override
        public void close() throws IOException {
            loxErrStream.close();
            loxOutStream.close();
            inputStream.close();
        }

        public String getOutput() {
            return loxOutStream.toString().replaceAll("\r\n", "\n");
        }
    }
}
