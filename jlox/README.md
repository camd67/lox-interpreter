# jlox

## Grammar
```
program -> declaration* EOF;

declaration -> varDecl | statement;
statement -> exprStatement
            | printStmt
            | block
            ;

block -> "{" declaration* "}";
exprStmt -> expression ";";
printStmt -> "print" expression ";";

varDecl -> "var" IDENTIFIER ("=" expression)?";";

expression -> assignment;
assignment -> (IDENTIFIER "=" assignment)? ternary;
ternary -> equality ( "?" ternary ":" ternary );
equality -> comparison ( ("!=" | "==") comparison )*;
comparison -> term ( (">" | ">=" | "<" | "<=" ) term )*;
term -> factor ( ("-" | "+" ) factor )*;
factor -> unary ( ("/" | "*") unary)*;
unary -> ("!" | "-") unary | primary;
primary -> NUMBER| STRING | "true" | "false" | "nil" | "(" expression ")";
```

