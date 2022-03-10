package ru.turbo.goose.utils;

import java.awt.*;

public class BoundaryChecker {
    public static Rectangle intersectInImageCoords(int imgW, int imgH, int x, int y, int w, int h) {
        Rectangle img = new Rectangle(0, 0, imgW, imgH);
        Rectangle seg = new Rectangle(x, y, w, h);
        return img.intersection(seg);
    }

    public static Rectangle intersectInSegmentCoords(int imgW, int imgH, int x, int y, int w, int h) {
        Rectangle img = new Rectangle(-x, -y, imgW, imgH);
        Rectangle seg = new Rectangle(0, 0, w, h);
        return img.intersection(seg);
    }
}
