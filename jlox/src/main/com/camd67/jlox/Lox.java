package com.camd67.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.IntConsumer;

public class Lox implements LoxGlobal {
    public static void main(String[] args) throws IOException {
        new Lox(System.in, System.out, System.err, System::exit).runFromCli(args);
    }

    boolean hadError;
    boolean hadRuntimeError;

    private final InputStream input;
    private final PrintStream output;
    private final PrintStream errOutput;
    private final IntConsumer exit;
    private final Interpreter interpreter;

    /**
     * Constructs a new lox
     */
    Lox(InputStream input, PrintStream output, PrintStream errOutput, IntConsumer exit) {
        this.input = input;
        this.output = output;
        this.errOutput = errOutput;
        this.exit = exit;
        interpreter= new Interpreter(this);
    }

    void runFromCli(String[] args) throws IOException {
        if (args.length > 1) {
            output.println("Usage: jlox [script]");
            exit.accept(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    void runFile(String filePath) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            exit.accept(65);
        }
        if (hadRuntimeError) {
            exit.accept(70);
        }
    }

    void runPrompt() throws IOException {
        var reader = new BufferedReader(new InputStreamReader(input));

        output.println("jlox REPL");
        output.println("CTRL + D to exit");
        output.println("-f <filename> to run a file in the lox dir (no ext)");
        output.println();

        while (true) {
            output.print("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }

            // handle file prompt-f
            if (line.startsWith("-f ")) {
                runFile("lox/" + line.substring(3) + ".lox");
                return;
            }

            run(line);
            // Clear our error each time we run a prompt.
            // Don't want a single error to corrupt our entire REPL
            hadError = false;
        }
    }

    private void run(String source) {
        var scanner = new Scanner(source, this);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens, this);
        var statements = parser.parse();

        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    @Override
    public void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    @Override
    public void error(int line, String message) {
        report(line, "", message);
    }

    private void report(int line, String where, String message) {
        errOutput.println("[line" + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    @Override
    public void runtimeError(RuntimeError error) {
        errOutput.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    @Override
    public void logOut(String message) {
        output.println(message);
    }

    @Override
    public void logErr(String message) {
        errOutput.println(message);
    }
}
