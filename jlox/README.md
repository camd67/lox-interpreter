# jlox

## Grammar

### v2

(with precedence)

```
expression -> ternary;
ternary -> equality ( "?" ternary ":" ternary );
equality -> comparison ( ("!=" | "==") comparison )*;
comparison -> term ( (">" | ">=" | "<" | "<=" ) term )*;
term -> factor ( ("-" | "+" ) factor )*;
factor -> unary ( ("/" | "*") unary)*;
unary -> ("!" | "-") unary | primary;
primary -> NUMBER| STRING | "true" | "false" | "nil" | "(" expression ")";
```

### v1

```
expression -> literal | unary | binary | grouping ;
literal -> NUMBER | STRING | "true" | "false" | "nil" ;
grouping -> "(" expression ")" ;
unary -> ( "-" | "!" ) expression ;
binary -> expression operator expression ;
operator -> "==" | "!=" | "<" | "<=" | ">" | ">=" | "+" | "-" | "*" | "/" ;
```