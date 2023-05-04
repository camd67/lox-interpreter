package com.camd67.jlox;

public interface LoxGlobal {
    void logOut(String message);

    void logErr(String message);

    void error(Token token, String message);

    void error(int line, String message);

    void runtimeError(RuntimeError error);
}
