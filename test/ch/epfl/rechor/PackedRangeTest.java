package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import static ch.epfl.rechor.PackedRange.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PackedRangeTest {

    @Test
    void packTest() {
        int a = 0x00ffffff; int b = 0x00ffffff;
        assertEquals(0xffffff00, pack(a, b));

        int c = 0x000000ff; int d = 0x0000010E;
        int e= d-c;
        assertEquals(0x0000ff00 + e, pack(c, d));

        int f = 0x0fffffff; int g = 0x00ffffff;
        assertThrows(IllegalArgumentException.class, () -> pack(f,g));

        int h = 0x0000ffff; int i = 0x0fffffff;
        assertThrows(IllegalArgumentException.class, () -> pack(h,i));
    }

    @Test
    void lengthTest() {
        int a = 0x000000ff;
        assertEquals(0xff, length(a));

        int b = 0xffffff00;
        assertEquals(0, length(b));

        int c = 0x00ff0ff0;
        assertEquals(0xf0, length(c));
    }

    @Test
    void startInclusiveTest() {
        int a = 0x000000ff;
        assertEquals(0, startInclusive(a));

        int b = 0xffffff00;
        assertEquals(0x00ffffff, startInclusive(b));

        int c = 0x00ff0ff0;
        assertEquals(0x00ff0f, startInclusive(c));
    }

    @Test
    void endExclusiveTest() {
        int a = 0x000000ff;
        assertEquals(0xff, endExclusive(a));

        int b = 0xffffff00;
        assertEquals(0xffffff, endExclusive(b));

        int c = 0x00ff0ff0;
        assertEquals(0x00ff0f + 0xf0, endExclusive(c));
    }
}
