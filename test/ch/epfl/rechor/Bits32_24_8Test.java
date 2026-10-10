package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import static ch.epfl.rechor.Bits32_24_8.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Bits32_24_8Test {

    @Test
    void unpack24Test() {
        int a = 0x000000ff;
        assertEquals(0, unpack24(a));
        int b = 0xffffff00;
        assertEquals(0x00ffffff, unpack24(b));
    }

    @Test
    void unpack8Test() {
        int a = 0x000000ff;
        assertEquals(0xff, unpack8(a));
        int b = 0xffffff00;
        assertEquals(0, unpack8(b));
    }

    @Test
    void packTest() {
        int a = 0x000000ff; int b = 0x00ffffff;
        assertEquals(0xffffffff, pack(b,a));
        int g = 0x0000000f; int h = 0x00ff0fff;
        assertEquals(0xff0fff0f, pack(h,g));
        int c=0x00000fff; int d = 0;
        assertThrows(IllegalArgumentException.class, () -> pack(d,c));
        int e=0x0fffffff; int f = 0;
        assertThrows(IllegalArgumentException.class, () -> pack(e,f));
    }
}
