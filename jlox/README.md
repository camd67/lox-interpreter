# jlox

## Grammar

```
program -> declaration* EOF;

declaration -> classDecl | funDecl | varDecl | statement;
statement -> exprStatement
            | forStmt
            | ifStmt
            | printStmt
            | returnStmt
            | whileStmt
            | breakStmt
            | block
            ;

returnStmt -> "return" expression? ";";
breakStmt -> "break" ";"
forStmt -> "for" "(" (varDecl | exprStmt | ";") expression? ";" expression? ")" statement ;
whileStmt -> "while" "(" expression ")" statement;
ifStmt -> "if" "(" expression ")" statement ( "else" statement )? ;
block -> "{" declaration* "}";
exprStmt -> expression ";";
printStmt -> "print" expression ";";

varDecl -> "var" IDENTIFIER ("=" expression)?";";

funDecl -> "fun" function;
function -> IDENTIFIER "(" parameters ? ")" block;
parameters -> IDENTIFIER ("<" IDENTIFIER)*;

classDecl -> "class" IDENTIFIER "{" function* "}";
function -> IDENTIFIER "(" parameters? ")" block;
parameters -> IDENTIFIER ( "," IDENTIFIER )*;

arguments -> expression ( "," expression )*;

expression -> assignment;
assignment -> (call "."? IDENTIFIER "=" assignment | logic_or;
logicOr -> logicAnd ("or" logicAnd)*;
logicAnd -> equality ("and" equality)*;
ternary -> equality ( "?" ternary ":" ternary );
equality -> comparison ( ("!=" | "==") comparison )*;
comparison -> term ( (">" | ">=" | "<" | "<=" ) term )*;
term -> factor ( ("-" | "+" ) factor )*;
factor -> unary ( ("/" | "*") unary)*;
unary -> ("!" | "-") unary | call;
call -> primary ( "(" arguments? ")" | "." IDENTIFIER )*;
primary -> NUMBER| STRING | "true" | "false" | "nil" | "(" expression ")";
```

