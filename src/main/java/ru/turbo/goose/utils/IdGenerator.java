package ru.turbo.goose.utils;

public class IdGenerator {
    private static int id = 1;

    public static int next() {
        return id++;
    }

    public static void reset() {
        id = 1;
    }
}
