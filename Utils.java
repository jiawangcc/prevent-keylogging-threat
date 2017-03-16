package com.example.jia.css539;

public class Utils {
    private static final int ASCII_PRINTABLE_CHARS_LOWER_BOUND = 32;
    private static final int ASCII_PRINTABLE_CHARS_UPPER_BOUND = 128;

    public static String toStr(int x) {
        return ((x >= ASCII_PRINTABLE_CHARS_LOWER_BOUND) && (x < ASCII_PRINTABLE_CHARS_UPPER_BOUND))
                ? String.valueOf((char) x)
                : "unprintable keyCode";
    }
}
