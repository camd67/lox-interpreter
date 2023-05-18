package com.camd67.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }

        var outputDir = args[0];

        defineAst(outputDir, "Expr", List.of(
            // This really aught to be more strongly typed
            "Assign : Token type, Expr value",
            "Binary: Expr left, Token operator, Expr right",
            "Call : Expr callee, Token paren, List<Expr> arguments",
            "Get : Expr object, Token name",
            "Set : Expr object, Token name, Expr value",
            "This : Token keyword",
            "Grouping: Expr expression",
            "Literal: Object value",
            "Unary: Token operator, Expr right",
            "Ternary: Expr check, Expr left, Expr right",
            "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", List.of(
            "Break : Token token",
            "While : Expr condition, Stmt body",
            "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Function : Token name, List<Token> params, List<Stmt> body",
            "Block : List<Stmt> statements",
            "Class : Token name, List<Stmt.Function> methods",
            "Expression : Expr expression",
            "Print : Expr expression",
            "Return : Token keyword, Expr value",
            "Var : Token name, Expr initializer"
        ));
    }

    private static void defineAst(
        String outputDir,
        String baseName,
        List<String> types
    ) throws IOException {
        var path = outputDir + "/" + baseName + ".java";
        try (var writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.println("/******************************");
            writer.println(" * THIS FILE IS AUTOGENERATED *");
            writer.println("******************************/");
            writer.println("package com.camd67.jlox;");
            writer.println();
            writer.println("import java.util.List;");
            writer.println();
            writer.println("abstract class " + baseName + " {");

            // Define visitor interface
            defineVisitor(writer, baseName, types);

            // The base class accept() method
            writer.println();
            writer.println("    abstract <R> R accept(Visitor<R> visitor);");

            // All the AST classes
            for (var type : types) {
                writer.println();
                var className = type.split(":")[0].trim();
                if (type.contains(":")) {
                    var fields = type.split(":")[1].trim();
                    defineType(writer, baseName, className, fields);
                } else {
                    defineType(writer, baseName, className, "");
                }
            }

            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (var type : types) {
            var typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
    }

    private static void defineType(
        PrintWriter writer,
        String baseName,
        String className,
        String fieldList
    ) {
        writer.println("    static class " + className + " extends " + baseName + " {");
        writer.println();

        String[] fields = new String[0];
        if (fieldList != "") {
            fields = fieldList.split(", ");
        }

        // Fields
        for (var field : fields) {
            writer.println("        final " + field + ";");
        }
        writer.println();

        // Constructor
        writer.println("        " + className + "(" + fieldList + ") {");
        for (var field : fields) {
            var name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        // Visitor!
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }
}
