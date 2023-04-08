package com.camd67.jlox;

public class LoxTestUtil {
    public static Lox mockLox() {
        return new Lox(System.in, System.out, System.err, System::exit);
    }
}
