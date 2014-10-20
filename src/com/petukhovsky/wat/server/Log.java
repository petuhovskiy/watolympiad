package com.petukhovsky.wat.server;

/**
 * Created by petuh_000 on 03.10.2014.
 */
public class Log {
    private static final boolean SHOW_DATA = false;

    public static void e(String s) {
        System.err.println(s);
    }

    public static void d(Object o) {
        System.out.println(o);
    }

    public static void c(String s) {
        if (SHOW_DATA) System.out.println(s);
    }

    public static void b(String s) {
        System.out.println("Chat: " + s);
    }
}
