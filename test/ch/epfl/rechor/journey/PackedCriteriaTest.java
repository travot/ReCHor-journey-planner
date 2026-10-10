package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PackedCriteriaTest {
    @Test
    void packedCriteriaPackWorksOnGivenExample() {
        var p = PackedCriteria.pack(9 * 60, 2, 0b1010_0101_1010_0101_1010_0101_1010_0101);
        assertEquals(0b0_000000000000_001100001100_0000010_1010_0101_1010_0101_1010_0101_1010_0101L, p);
    }

    @Test
    void packedCriteriaPackThrowsWithInvalidArrivalTimes() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(-4 * 60 - 1, 0, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(48 * 60, 0, 0);
        });
    }

    @Test
    void packedCriteriaPackThrowsWithInvalidChanges() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(0, -1, 0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.pack(0, 128, 0);
        });
    }

    @Test
    void packedCriteriaPackWorksForAllValidArrivalTimes() {
        for (var h = -4; h < 48; h += 1) {
            for (var m = 0; m < 60; m += 1) {
                var expArrMins = h * 60 + m;
                var p = PackedCriteria.pack(expArrMins, 0, 0);
                var actArrMins = PackedCriteria.arrMins(p);
                assertEquals(expArrMins, actArrMins);
            }
        }
    }

    @Test
    void packedCriteriaPackWorksForAllValidChanges() {
        for (var expChanges = 0; expChanges < 128; expChanges += 1) {
            var p = PackedCriteria.pack(0, expChanges, 0);
            var actChanges = PackedCriteria.changes(p);
            assertEquals(expChanges, actChanges);
        }
    }

    @Test
    void packedCriteriaPackWorksWithManyPayloads() {
        for (var expPayload = 1; expPayload != 0 ; expPayload <<= 1) {
            var p = PackedCriteria.pack(0, 0, expPayload);
            var actPayload = PackedCriteria.payload(p);
            assertEquals(expPayload, actPayload);
        }
        assertEquals(0, PackedCriteria.payload(PackedCriteria.pack(0, 0, 0)));
    }

    @Test
    void packedCriteriaPackLeavesDepMinsEmpty() {
        assertFalse(PackedCriteria.hasDepMins(PackedCriteria.pack(1, 2, 3)));
    }

    @Test
    void packedCriteriaDepMinsThrowsWithoutDepMins() {
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.depMins(PackedCriteria.pack(1, 2, 3));
        });
    }

    @Test
    void packedCriteriaWithDepMinsWorksForAllValidDepartureTimes() {
        for (var h = -4; h < 48; h += 1) {
            for (var m = 0; m < 60; m += 1) {
                var expDepMins = h * 60 + m;
                var p = PackedCriteria.withDepMins(
                        PackedCriteria.pack(0, 0, 0),
                        expDepMins);
                var actDepMins = PackedCriteria.depMins(p);
                assertEquals(expDepMins, actDepMins);
            }
        }
    }

    @Test
    void packedCriteriaDominatesOrIsEqualThrowsWithIncompatibleTuples() {
        var t1 = PackedCriteria.pack(0, 0, 0);
        var t2 = PackedCriteria.withDepMins(t1, 0);
        assertThrows(IllegalArgumentException.class, () -> {
            PackedCriteria.dominatesOrIsEqual(t1, t2);
        });
    }

    @Test
    void packedCriteriaDominatesOrIsEqualWorksWithEqualCriteria() {
        var t1 = PackedCriteria.pack(5, 1, 1);
        var t2 = PackedCriteria.pack(5, 1, 2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(t1, t2));
        assertTrue(PackedCriteria.dominatesOrIsEqual(t2, t1));

        var t3 = PackedCriteria.withDepMins(t1, 10);
        var t4 = PackedCriteria.withDepMins(t2, 10);
        assertTrue(PackedCriteria.dominatesOrIsEqual(t3, t4));
        assertTrue(PackedCriteria.dominatesOrIsEqual(t4, t3));
    }

    @Test
    void packedCriteriaDominatesOrIsEqualWorksWithDominatingCriteria() {
        // 1. lower arrival time
        var o1 = PackedCriteria.pack(5, 1, 1);
        var u1 = PackedCriteria.pack(6, 1, 2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(o1, u1));
        assertFalse(PackedCriteria.dominatesOrIsEqual(u1, o1));

        // 2. fewer changes
        var o2 = PackedCriteria.pack(9, 1, 1);
        var u2 = PackedCriteria.pack(9, 2, 2);
        assertTrue(PackedCriteria.dominatesOrIsEqual(o2, u2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(u2, o2));

        // 3. later departure time
        var o3 = PackedCriteria.withDepMins(o1, 200);
        var u3 = PackedCriteria.withDepMins(o1, 199);
        assertTrue(PackedCriteria.dominatesOrIsEqual(o3, u3));
        assertFalse(PackedCriteria.dominatesOrIsEqual(u3, o3));
    }

    @Test
    void packedCriteriaDominatesOrIsEqualWorksWithUncomparableCriteria() {
        var t1 = PackedCriteria.pack(5, 2, 1);
        var t2 = PackedCriteria.pack(6, 1, 2);
        assertFalse(PackedCriteria.dominatesOrIsEqual(t1, t2));
        assertFalse(PackedCriteria.dominatesOrIsEqual(t2, t1));

        var t3 = PackedCriteria.withDepMins(
                PackedCriteria.pack(10, 1, 5), 3);
        var t4 = PackedCriteria.withDepMins(
                PackedCriteria.pack(10, 2, 5), 4);
        assertFalse(PackedCriteria.dominatesOrIsEqual(t3, t4));
        assertFalse(PackedCriteria.dominatesOrIsEqual(t4, t3));
    }

    @Test
    void packedCriteriaWithoutDepMinsRemovesDepMins() {
        var t = PackedCriteria.pack(1, 2, 3);
        t = PackedCriteria.withDepMins(t, 5);
        assertTrue(PackedCriteria.hasDepMins(t));
        t = PackedCriteria.withoutDepMins(t);
        assertFalse(PackedCriteria.hasDepMins(t));
    }

    @Test
    void packedCriteriaWithAdditionalChangeIncrementsChanges() {
        var t = PackedCriteria.pack(1, 0, 3);
        for (int i = 0; i < 127; i += 1) {
            assertEquals(i, PackedCriteria.changes(t));
            t = PackedCriteria.withAdditionalChange(t);
        }
        assertEquals(127, PackedCriteria.changes(t));
    }

    @Test
    void packedCriteriaWithPayloadWorks() {
        var rng = new Random(2025);
        var depMins = rng.nextInt(-4 * 60, 48 * 60);
        var arrMins = rng.nextInt(-4 * 60, 48 * 60);
        var changes = rng.nextInt(128);
        var t = PackedCriteria.withDepMins(
                PackedCriteria.pack(arrMins, changes, 0), depMins);
        for (int i = 0; i < 100; i += 1) {
            var newPayload = rng.nextInt();
            t = PackedCriteria.withPayload(t, newPayload);
            assertEquals(depMins, PackedCriteria.depMins(t));
            assertEquals(arrMins, PackedCriteria.arrMins(t));
            assertEquals(changes, PackedCriteria.changes(t));
            assertEquals(newPayload, PackedCriteria.payload(t));
        }
    }
}