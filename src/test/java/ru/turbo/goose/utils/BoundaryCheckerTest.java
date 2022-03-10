package ru.turbo.goose.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class BoundaryCheckerTest {
    @Nested
    class IntersectInImageCoordsMethodTests {
        @Test
        public void whenImageMinXAndMinYAboveZero() {
            Rectangle expected = new Rectangle(10, 10, 30, 30);
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, 10, 10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXAndMinYBelowZero() {
            Rectangle expected = new Rectangle(0, 0, 20, 20);
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, -10, -10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXBelowZeroAndMinYAboveZero() {
            Rectangle expected = new Rectangle(0, 10, 20, 30);
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, -10, 10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXAboveZeroAndMinYBelowZero() {
            Rectangle expected = new Rectangle(10, 0, 30, 20);
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, 10, -10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenSegmentContainingImage() {
            Rectangle expected = new Rectangle(0, 0, 100, 100);
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, -10, -10, 300, 300);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageAndSegmentDoNotIntersect() {
            Rectangle actual = BoundaryChecker.intersectInImageCoords(100, 100, -100, -100, 10, 10);
            assertThat(actual.isEmpty(), is(true));
        }
    }

    @Nested
    class IntersectInSegmentCoordsMethodTests {
        @Test
        public void whenImageMinXAndMinYAboveZero() {
            Rectangle expected = new Rectangle(0, 0, 30, 30);
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, 10, 10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXAndMinYBelowZero() {
            Rectangle expected = new Rectangle(10, 10, 20, 20);
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, -10, -10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXBelowZeroAndMinYAboveZero() {
            Rectangle expected = new Rectangle(10, 0, 20, 30);
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, -10, 10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageMinXAboveZeroAndMinYBelowZero() {
            Rectangle expected = new Rectangle(0, 10, 30, 20);
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, 10, -10, 30, 30);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenSegmentContainingImage() {
            Rectangle expected = new Rectangle(10, 10, 100, 100);
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, -10, -10, 300, 300);
            assertThat(actual, is(expected));
        }

        @Test
        public void whenImageAndSegmentDoNotIntersect() {
            Rectangle actual = BoundaryChecker.intersectInSegmentCoords(100, 100, -100, -100, 10, 10);
            assertThat(actual.isEmpty(), is(true));
        }
    }
}