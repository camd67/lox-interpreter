package com.camd67.jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {
    private static boolean hadError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filePath) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(filePath));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        System.out.println("jlox REPL");
        System.out.println("CTRL + D to exit");
        System.out.println("-f <filename> to run a file in the lox dir (no ext)");
        System.out.println();

        while (true) {
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }

            // handle file prompt-f
            if (line.startsWith("-f ")) {
                runFile("lox/" + line.substring(2) + ".lox");
                return;
            }

            run(line);
            // Clear our error each time we run a prompt.
            // Don't want a single error to corrupt our entire REPL
            hadError = false;
        }
    }

    private static void run(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens);
        var expression = parser.parse();

        if (hadError) {
            return;
        }

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line" + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
