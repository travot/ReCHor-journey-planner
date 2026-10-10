package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParetoFrontTest {
    @Test
    void paretoFrontEmptyIsEmpty() {
        assertEquals(0, ParetoFront.EMPTY.size());
        ParetoFront.EMPTY.forEach(d -> fail());
    }

    @Test
    void paretoFrontGetWorksWithExistingTuples() {
        // Without departure time
        var b2d = new ParetoFront.Builder();
        for (var i = 0; i < 20; i += 1)
            b2d.add(PackedCriteria.pack(10 + i, 127 - i, i));

        var f2d = b2d.build();
        for (var i = 0; i < 20; i += 1) {
            var arrMins = 10 + i;
            var changes = 127 - i;
            var expected = PackedCriteria.pack(arrMins, changes, i);
            assertEquals(expected, f2d.get(arrMins, changes));
        }

        // With departure time
        var b3d = new ParetoFront.Builder();
        for (var i = 0; i < 20; i += 1) {
            b3d.add(PackedCriteria.withDepMins(
                    PackedCriteria.pack(10 + i, 127 - i, i),
                    i - 10));
        }

        var f3d = b3d.build();
        for (var i = 0; i < 20; i += 1) {
            var depMins = i - 10;
            var arrMins = 10 + i;
            var changes = 127 - i;
            var expected = PackedCriteria.withDepMins(
                    PackedCriteria.pack(arrMins, changes, i),
                    depMins);
            assertEquals(expected, f3d.get(arrMins, changes));
        }
    }

    @Test
    void paretoFrontGetThrowsWithNonExistingTuples() {
        var b2d = new ParetoFront.Builder();
        for (var i = 0; i < 20; i += 1)
            b2d.add(PackedCriteria.pack(10 + i, 127 - i, i));

        var f2d = b2d.build();
        for (var i = 0; i < 20; i += 1) {
            var arrMins = 10 + i;
            var changes = 127 - i;
            assertThrows(NoSuchElementException.class, () -> {
                f2d.get(arrMins + 1, changes);
            });
            assertThrows(NoSuchElementException.class, () -> {
                f2d.get(arrMins, changes - 1);
            });
        }
    }

    @Test
    void paretoFrontBuilderIsEmptyIsInitiallyTrue() {
        var b = new ParetoFront.Builder();
        assertTrue(b.isEmpty());
    }

    @Test
    void paretoFrontBuilderCopyConstructorCopiesArray() {
        var b1 = new ParetoFront.Builder();
        b1.add(PackedCriteria.pack(1, 2, 3));

        var b2 = new ParetoFront.Builder(b1);
        b2.add(PackedCriteria.pack(2, 1, 3));

        assertEquals(2, b2.build().size());
        assertEquals(1, b1.build().size());
    }

    @Test
    void paretoFrontBuilderAddCanAdd0() {
        var b = new ParetoFront.Builder();
        var zero = 0L; // a.k.a. PackedCriteria.pack(-240, 0, 0);
        b.add(zero);
        var f = b.build();
        assertEquals(1, f.size());
        assertEquals(zero, f.get(-240, 0));
    }

    @Test
    void paretoFrontBuilderAddWorksWithMinMaxPayload() {
        var f = new ParetoFront.Builder()
                .add(10, 2, 2025)
                .add(20, 1, 10)
                .add(10, 2, 0)
                .add(30, 0, 20)
                .add(10, 2, ~0)
                .build();

        assertEquals(3, f.size());
        var payload = PackedCriteria.payload(f.get(10, 2));
        assertTrue(payload == 0 || payload == 2025 || payload == ~0);
    }

    @Test
    void paretoFrontBuilderAddCanAddManyTuples() {
        var b = new ParetoFront.Builder();
        for (var i = 0; i < 128; i += 1)
            b.add(PackedCriteria.pack(10 + i, 127 - i, i));
        assertEquals(128, b.build().size());
    }

    @Test
    void paretoFrontBuilderAddCanRemoveManyTuples() {
        var b = new ParetoFront.Builder();
        for (var i = 0; i < 128; i += 1)
            b.add(PackedCriteria.pack(10 + i, 127 - i, i));
        var dominator = PackedCriteria.pack(9, 0, 0);
        b.add(dominator);
        var f = b.build();
        assertEquals(1, f.size());
        f.forEach(d -> assertEquals(dominator, d));
    }

    @Test
    void paretoFrontBuilderCopiesArray() {
        var b = new ParetoFront.Builder();
        b.add(PackedCriteria.pack(1, 2, 3));

        var f = b.build();
        assertEquals(1, f.size());
        b.add(PackedCriteria.pack(2, 1, 3));

        assertEquals(1, f.size());
    }

    @Test
    void paretoFrontBuilderAddCorrectlyPacksCriteria() {
        for (int arrMins = 100; arrMins < 120; arrMins += 1) {
            for (int changes = 0; changes < 10; changes += 1) {
                for (int payload = 2020; payload < 2025; payload += 1) {
                    var b = new ParetoFront.Builder();
                    var finalArrMins = arrMins;
                    var finalChanges = changes;
                    var finalPayload = payload;
                    b.add(finalArrMins, finalChanges, finalPayload);
                    b.forEach(p -> {
                        assertEquals(finalArrMins, PackedCriteria.arrMins(p));
                        assertEquals(finalChanges, PackedCriteria.changes(p));
                        assertEquals(finalPayload, PackedCriteria.payload(p));
                    });
                }
            }
        }
    }

    @Test
    void paretoFrontBuilderAddAllWorks() {
        var b1 = new ParetoFront.Builder();
        b1.add(20, 0, 1); // t1
        b1.add(17, 1, 2); // t2
        b1.add(15, 2, 3); // t3
        b1.add(13, 3, 4); // t4

        var b2 = new ParetoFront.Builder();
        b2.add(17, 0, 5); // t5, dominates t1 and t2
        b2.add(15, 3, 6); // t6, dominated by t3
        b2.add(11, 4, 7); // t7

        b1.addAll(b2);
        var b = b1.build();
        assertEquals(4, b.size());
        assertEquals(3, PackedCriteria.payload(b.get(15, 2)));
        assertEquals(4, PackedCriteria.payload(b.get(13, 3)));
        assertEquals(5, PackedCriteria.payload(b.get(17, 0)));
        assertEquals(7, PackedCriteria.payload(b.get(11, 4)));
    }

    @Test
    void paretoFrontBuilderFullyDominatesWorks() {
        var b1 = new ParetoFront.Builder();
        add(b1, 50, 60, 5, 1);
        add(b1, 50, 65, 4, 2);
        add(b1, 50, 70, 3, 3);

        var b2 = new ParetoFront.Builder();
        b2.add(60, 5, 1);
        b2.add(65, 4, 2);
        b2.add(70, 3, 3);

        assertTrue(b1.fullyDominates(b2, 49));
        assertTrue(b1.fullyDominates(b2, 50));
        assertFalse(b1.fullyDominates(b2, 51));
    }

    private static void add(ParetoFront.Builder builder, int depMins, int arrMins, int changes, int payload) {
        builder.add(PackedCriteria.withDepMins(
                PackedCriteria.pack(arrMins, changes, payload),
                depMins));
    }

    @Test
    void paretoFrontBuilderAddWorksLikeNaiveParetoFrontBuilder() {
        var rng = new Random(2025);

        var naiveParetoFrontB = new NaiveParetoFrontBuilder();
        var paretoFrontB = new ParetoFront.Builder();

        for (int i = 0; i < 1000; i += 1) {
            // Clear builders with a 1% probability
            if (rng.nextInt(100) == 0) {
                naiveParetoFrontB.clear();
                paretoFrontB.clear();
            }

            var arrMins = rng.nextInt(1000, 2000);
            var depMins = rng.nextInt(0, arrMins);
            var changes = rng.nextInt(128);
            var t = PackedCriteria.pack(arrMins, changes, 0);
            t = PackedCriteria.withDepMins(t, depMins);

            naiveParetoFrontB.add(t);
            paretoFrontB.add(t);

            var expectedTuples = naiveParetoFrontB.toSortedList();
            var actualTuples = toSortedList(paretoFrontB);
            assertEquals(expectedTuples, actualTuples);
        }
    }

    private static List<Long> toSortedList(ParetoFront.Builder paretoFrontB) {
        var sortedTuples = new ArrayList<Long>();
        paretoFrontB.forEach(t -> sortedTuples.add(t));
        Collections.sort(sortedTuples);
        return sortedTuples;
    }

    // An inefficient (but obviously correct) implementation of a Pareto frontier
    static class NaiveParetoFrontBuilder {
        private final List<Long> tuples = new ArrayList<>();

        public void add(long newTuple) {
            var tupleIt = tuples.iterator();
            while (tupleIt.hasNext()) {
                var tuple = (long) tupleIt.next();
                if (PackedCriteria.dominatesOrIsEqual(tuple, newTuple)) return;
                if (PackedCriteria.dominatesOrIsEqual(newTuple, tuple)) tupleIt.remove();
            }
            tuples.add(newTuple);
        }

        public int size() {
            return tuples.size();
        }

        public void clear() {
            tuples.clear();
        }

        // Returns the tuples of the frontier as a sorted array of longs.
        public List<Long> toSortedList() {
            var sortedTuples = new ArrayList<>(tuples);
            Collections.sort(sortedTuples);
            return sortedTuples;
        }

        @Override
        public String toString() {
            var j = new StringJoiner(",", "{", "}");
            for (var t : tuples) j.add(tupleToString(t));
            return j.toString();
        }

        private static String formatMins(int mins) {
            return "%02d:%02d".formatted(
                    Math.floorDiv(mins, 60),
                    Math.floorMod(mins, 60));
        }

        private static String tupleToString(long pTuple) {
            return String.format("%s%s/%d",
                    PackedCriteria.hasDepMins(pTuple)
                            ? formatMins(PackedCriteria.depMins(pTuple)) + "-"
                            : "",
                    formatMins(PackedCriteria.arrMins(pTuple)),
                    PackedCriteria.changes(pTuple));
        }
    }
}