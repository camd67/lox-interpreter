package com.camd67.jlox;

/**
 * Exception to (grossly) unwind the stack during a
 * lox return statement.
 * Value can be included to return a value
 */
public class Return extends RuntimeException {
    final Object value;

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
