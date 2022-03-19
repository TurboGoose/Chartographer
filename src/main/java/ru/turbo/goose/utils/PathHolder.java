package ru.turbo.goose.utils;

public class PathHolder {
    private static String path;

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        PathHolder.path = path;
    }
}
