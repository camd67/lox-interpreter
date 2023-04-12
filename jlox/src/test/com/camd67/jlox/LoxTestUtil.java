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

    public static String loxOutput(String out) {
        // Normalize our line endings so that tests pass.
        // PrintStream outputs system line endings by default.
        return out.replaceAll("\n", System.lineSeparator());
    }

    /**
     * An instance of lox that sends all output and reads all input from test
     * local variables.
     * Read those variables for assertions.
     */
    public static class TestLox implements AutoCloseable {

        private static final IntConsumer defaultOnExit = (int i) ->
            assertEquals(0, i, "Exit called with a non-zero code");

        public final OutputStream loxOutStream = new ByteArrayOutputStream();
        public final OutputStream loxErrStream = new ByteArrayOutputStream();
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
            lox = new Lox(inputStream, new PrintStream(loxOutStream), new PrintStream(loxErrStream), onExit);
        }

        public void assertNoErrOutput() {
            assertEquals("", loxErrStream.toString());
        }

        @Override
        public void close() throws IOException {
            loxErrStream.close();
            loxOutStream.close();
            inputStream.close();
        }
    }
}
