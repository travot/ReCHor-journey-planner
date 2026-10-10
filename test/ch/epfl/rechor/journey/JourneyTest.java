package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JourneyTest {
    private static List<Journey.Leg> exampleLegs() {
        var s1 = new Stop("Ecublens VD, EPFL", null, 6.566141, 46.522196);
        var s2 = new Stop("Renens VD, gare", null, 6.578519, 46.537619);
        var s3 = new Stop("Renens VD", "4", 6.578935, 46.537042);
        var s4 = new Stop("Lausanne", "5", 6.629092, 46.516792);
        var s5 = new Stop("Lausanne", "1", 6.629092, 46.516792);
        var s6 = new Stop("Romont FR", "2", 6.911811, 46.693508);

        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var l1 = new Journey.Leg.Transport(
                s1,
                d.atTime(16, 13),
                s2,
                d.atTime(16, 19),
                List.of(),
                Vehicle.METRO,
                "m1",
                "Renens VD, gare");

        var l2 = new Journey.Leg.Foot(s2, d.atTime(16, 19), s3, d.atTime(16, 22));

        var l3 = new Journey.Leg.Transport(
                s3,
                d.atTime(16, 26),
                s4,
                d.atTime(16, 33),
                List.of(),
                Vehicle.TRAIN,
                "R4",
                "Bex");

        var l4 = new Journey.Leg.Foot(s4, d.atTime(16, 33), s5, d.atTime(16, 38));

        var l5 = new Journey.Leg.Transport(
                s5,
                d.atTime(16, 40),
                s6,
                d.atTime(17, 13),
                List.of(),
                Vehicle.TRAIN,
                "IR15",
                "Luzern");

        return List.of(l1, l2, l3, l4, l5);
    }

    @Test
    void journeyConstructorThrowsWhenThereAreNoLegs() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey(List.of());
        });
    }

    @Test
    void journeyConstructorThrowsWhenLegsDoNotAlternate() {
        var legs = new ArrayList<>(exampleLegs());
        var l2 = legs.get(1);
        legs.add(2, new Journey.Leg.Foot(l2.arrStop(), l2.arrTime(), l2.arrStop(), l2.arrTime().plusMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey(legs);
        });
    }

    @Test
    void journeyConstructorThrowsWhenLegsAreNotConsecutive() {
        var legs = new ArrayList<>(exampleLegs());
        legs.remove(2);
        legs.remove(3);
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey(legs);
        });
    }

    @Test
    void journeyConstructorCopiesLegs() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s = new Stop("A", null, 7, 47);
        var dT = d.atTime(8, 0);
        var l = (Journey.Leg) new Journey.Leg.Foot(s, dT, s, dT.plusMinutes(5));
        var immutableLegs = List.of(l);
        var mutableLegs = new ArrayList<>(immutableLegs);
        var j = new Journey(mutableLegs);
        mutableLegs.clear();
        assertEquals(immutableLegs, j.legs());
    }

    @Test
    void journeyConstructorCopiesLegsInImmutableList() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("A", null, 7, 47);
        var dT = d.atTime(8, 0);
        var l = (Journey.Leg) new Journey.Leg.Foot(s1, dT, s1, dT.plusMinutes(5));
        var immutableLegs = List.of(l);
        var j = new Journey(immutableLegs);
        assertThrows(UnsupportedOperationException.class, () -> {
            j.legs().clear();
        });
    }

    @Test
    void journeyDepStopWorks() {
        var legs = exampleLegs();
        assertEquals(legs.getFirst().depStop(), new Journey(legs).depStop());
    }

    @Test
    void journeyArrStopWorks() {
        var legs = exampleLegs();
        assertEquals(legs.getLast().arrStop(), new Journey(legs).arrStop());
    }

    @Test
    void journeyDepTimeWorks() {
        var legs = exampleLegs();
        assertEquals(legs.getFirst().depTime(), new Journey(legs).depTime());
    }

    @Test
    void journeyArrTimeWorks() {
        var legs = exampleLegs();
        assertEquals(legs.getLast().arrTime(), new Journey(legs).arrTime());
    }

    @Test
    void journeyDurationWorks() {
        var legs = exampleLegs();
        var j = new Journey(legs);
        assertEquals(Duration.ofMinutes(60), j.duration());
    }

    @Test
    void journeyIntermediateStopConstructorWorksWhenEverythingIsOk() {
        var s = new Stop("Lausanne", "70", 7, 47);
        var d0 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 0);
        var d1 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 1);
        var is = new Journey.Leg.IntermediateStop(s, d0, d1);
        assertEquals(s, is.stop());
        assertEquals(d1, is.depTime());
        assertEquals(d0, is.arrTime());
    }

    @Test
    void journeyIntermediateStopConstructorThrowsWhenStopIsNull() {
        var d0 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 0);
        var d1 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 1);
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.IntermediateStop(null, d0, d1);
        });
    }

    @Test
    void journeyIntermediateStopConstructorThrowsWhenTimesAreSwapped() {
        var d0 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 0);
        var d1 = LocalDate.of(2025, Month.FEBRUARY, 18).atTime(8, 1);
        var s = new Stop("Lausanne", "70", 7, 47);
        new Journey.Leg.IntermediateStop(s, d0, d0); // Same time is ok
        new Journey.Leg.IntermediateStop(s, d1, d1); // Same time is ok
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey.Leg.IntermediateStop(s, d1, d0);
        });
    }

    @Test
    void journeyLegDurationWorks() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var dS = new Stop("A", null, 7, 47);
        var aS = new Stop("B", null, 8, 48);
        var dT = d.atTime(8, 0);
        var r = "IC 1";
        var h = "Rorschach";
        for (int m = 0; m < 120; m += 1) {
            var aT = dT.plusMinutes(m);

            var f = new Journey.Leg.Foot(dS, dT, aS, aT);
            assertEquals(Duration.ofMinutes(m), f.duration());

            var t = new Journey.Leg.Transport(dS, dT, aS, aT, List.of(), Vehicle.TRAIN, r, h);
            assertEquals(Duration.ofMinutes(m), t.duration());
        }
    }

    @Test
    void journeyLegTransportConstructorThrowsWhenSomeArgumentIsNull() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var s3 = new Stop("Ecublens VD, EPFL", null, 8, 48);
        var s4 = new Stop("Chavannes-R., UNIL-Mouline", null, 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);
        var r = "IC 1";
        var h = "Rorschach";

        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(null, dT, s4, aT, List.of(), Vehicle.TRAIN, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, null, s4, aT, List.of(), Vehicle.TRAIN, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, null, aT, List.of(), Vehicle.TRAIN, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, s4, null, List.of(), Vehicle.TRAIN, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, s4, aT, null, Vehicle.TRAIN, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, s4, aT, List.of(), null, r, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, s4, aT, List.of(), Vehicle.TRAIN, null, h);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Transport(s1, dT, s4, aT, List.of(), Vehicle.TRAIN, r, null);
        });
    }

    @Test
    void journeyLegTransportConstructorThrowsWhenTimesAreSwapped() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var s3 = new Stop("Ecublens VD, EPFL", null, 8, 48);
        var s4 = new Stop("Chavannes-R., UNIL-Mouline", null, 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);
        var r = "IC 1";
        var h = "Rorschach";

        new Journey.Leg.Transport(s1, dT, s4, dT, List.of(), Vehicle.TRAIN, r, h); // Same time is ok
        new Journey.Leg.Transport(s1, aT, s4, aT, List.of(), Vehicle.TRAIN, r, h); // Same time is ok
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey.Leg.Transport(s1, aT, s4, dT, List.of(), Vehicle.TRAIN, r, h);
        });
    }

    @Test
    void journeyLegTransportConstructorCopiesIntermediateStops() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var s3 = new Stop("Ecublens VD, EPFL", null, 8, 48);
        var s4 = new Stop("Chavannes-R., UNIL-Mouline", null, 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);
        var r = "IC 1";
        var h = "Rorschach";

        var immutableStops = List.of(
                new Journey.Leg.IntermediateStop(s2, dT.plusMinutes(10), dT.plusMinutes(15)),
                new Journey.Leg.IntermediateStop(s3, dT.plusMinutes(20), dT.plusMinutes(25)));
        var mutableStops = new ArrayList<>(immutableStops);
        var l = new Journey.Leg.Transport(s1, dT, s4, aT, mutableStops, Vehicle.TRAIN, r, h);
        mutableStops.clear();
        assertEquals(immutableStops, l.intermediateStops());
    }

    @Test
    void journeyLegTransportConstructorCopiesIntermediateStopsInImmutableList() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var s3 = new Stop("Ecublens VD, EPFL", null, 8, 48);
        var s4 = new Stop("Chavannes-R., UNIL-Mouline", null, 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);
        var r = "IC 1";
        var h = "Rorschach";

        var immutableStops = List.of(
                new Journey.Leg.IntermediateStop(s2, dT.plusMinutes(10), dT.plusMinutes(15)),
                new Journey.Leg.IntermediateStop(s3, dT.plusMinutes(20), dT.plusMinutes(25)));
        var l = new Journey.Leg.Transport(s1, dT, s4, aT, immutableStops, Vehicle.TRAIN, r, h);
        assertThrows(UnsupportedOperationException.class, () -> {
            l.intermediateStops().clear();
        });
    }

    @Test
    void journeyLegFootConstructorThrowsWhenSomeArgumentIsNull() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);

        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Foot(null, dT, s2, aT);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Foot(s1, null, s2, aT);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Foot(s1, dT, null, aT);
        });
        assertThrows(NullPointerException.class, () -> {
            new Journey.Leg.Foot(s1, dT, s2, null);
        });
    }

    @Test
    void journeyLegFootConstructorThrowsWhenTimesAreSwapped() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);

        new Journey.Leg.Foot(s1, dT, s2, dT); // Same time is ok
        new Journey.Leg.Foot(s1, aT, s2, aT); // Same time is ok
        assertThrows(IllegalArgumentException.class, () -> {
            new Journey.Leg.Foot(s1, aT, s2, dT);
        });
    }

    @Test
    void journeyLegFootIntermediateStopsIsEmpty() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("A", null, 7, 47);
        var dT = d.atTime(8, 0);
        var l = new Journey.Leg.Foot(s1, dT, s1, dT.plusMinutes(5));
        assertTrue(l.intermediateStops().isEmpty());
    }

    @Test
    void journeyLegFootIsTransferWorks() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("A", null, 7, 47);
        var s2 = new Stop("A", "1", 7, 47);
        var s3 = new Stop("B", null, 8, 48);
        var dT = d.atTime(8, 0);
        assertTrue(new Journey.Leg.Foot(s1, dT, s1, dT.plusMinutes(5)).isTransfer());
        assertTrue(new Journey.Leg.Foot(s1, dT, s2, dT.plusMinutes(5)).isTransfer());
        assertTrue(new Journey.Leg.Foot(s2, dT, s1, dT.plusMinutes(5)).isTransfer());
        assertTrue(new Journey.Leg.Foot(s3, dT, s3, dT.plusMinutes(5)).isTransfer());
        assertFalse(new Journey.Leg.Foot(s1, dT, s3, dT.plusMinutes(5)).isTransfer());
        assertFalse(new Journey.Leg.Foot(s2, dT, s3, dT.plusMinutes(5)).isTransfer());
        assertFalse(new Journey.Leg.Foot(s3, dT, s1, dT.plusMinutes(5)).isTransfer());
    }
}