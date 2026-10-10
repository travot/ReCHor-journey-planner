package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatterFrTest {
    @Test
    void formatterFrFormatDurationWorksWithLessThanSixtyMinutes() {
        for (var m = 0; m < 60; m += 1) {
            var d = Duration.ofMinutes(m);
            assertEquals(m + " min", FormatterFr.formatDuration(d));
        }
    }

    @Test
    void formatterFrFormatDurationWorksWithMoreThanSixtyMinutes() {
        for (var h = 1; h < 40; h += 1) {
            var dH = Duration.ofHours(h);
            for (var m = 0; m < 60; m += 1) {
                var d = dH.plusMinutes(m);
                var expected = h + " h " + m + " min";
                assertEquals(expected, FormatterFr.formatDuration(d));
            }
        }
    }

    @Test
    void formatterFrFormatTimeWorksForWholeDay() {
        var date = LocalDate.of(2025, Month.FEBRUARY, 18);
        for (var h = 0; h < 24; h += 1) {
            for (var m = 0; m < 60; m += 1) {
                var dt = date.atTime(h, m);
                var expected = h + "h" + (m < 10 ? "0" : "") + m;
                assertEquals(expected, FormatterFr.formatTime(dt));
            }
        }
    }

    @Test
    void formatterFrFormatPlatformNameWorksWithNullName() {
        var stop = new Stop("EPFL", null, 0, 0);
        assertEquals("", FormatterFr.formatPlatformName(stop));
    }

    @Test
    void formatterFrFormatPlatformNameWorksWithEmptyName() {
        var stop = new Stop("EPFL", "", 0, 0);
        assertEquals("", FormatterFr.formatPlatformName(stop));
    }

    @Test
    void formatterFrFormatPlatformNameWorksWithNumericPlatform() {
        var sectors = List.of("", "AB", "C");
        for (var i = 0; i < 10; i += 1) {
            var platformName = i + sectors.get(i % sectors.size());
            var stop = new Stop("EPFL", platformName, 7, 47);
            assertEquals("voie " + platformName, FormatterFr.formatPlatformName(stop));
        }
    }

    @Test
    void formatterFrFormatPlatformNameWorksWithAlphabeticPlatform() {
        for (var i = 'A'; i <= 'Z'; i += 1) {
            var stop = new Stop("EPFL", String.valueOf(i), 7, 47);
            assertEquals("quai " + i, FormatterFr.formatPlatformName(stop));
        }
    }

    @Test
    void formatterFrFormatLegWorksWithFootLeg() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("A", null, 7, 47);
        var s1b = new Stop("A", "1", 7, 47);
        var s2 = new Stop("B", null, 8, 48);
        var dT = d.atTime(8, 0);
        var l1 = new Journey.Leg.Foot(s1, dT, s2, dT.plusMinutes(5));
        var l2 = new Journey.Leg.Foot(s1, dT, s1b, dT.plusMinutes(3));
        assertEquals("trajet à pied (5 min)", FormatterFr.formatLeg(l1));
        assertEquals("changement (3 min)", FormatterFr.formatLeg(l2));
    }

    @Test
    void formatterFrFormatLegWorksWithTransportLeg() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var s1 = new Stop("Lausanne", "70", 7, 47);
        var s2 = new Stop("Palézieux", "1", 8, 48);
        var s3 = new Stop("Ecublens VD, EPFL", null, 8, 48);
        var s4 = new Stop("Chavannes-R., UNIL-Mouline", null, 8, 48);
        var dT = d.atTime(8, 5);
        var aT = d.atTime(9, 12);
        var r = "IC 1";
        var h = "Rorschach";

        var l1 = new Journey.Leg.Transport(s1, dT, s2, aT, List.of(), Vehicle.TRAIN, r, h);
        assertEquals("8h05 Lausanne (voie 70) → Palézieux (arr. 9h12 voie 1)", FormatterFr.formatLeg(l1));

        var l2 = new Journey.Leg.Transport(s3, dT, s4, aT, List.of(), Vehicle.TRAIN, r, h);
        assertEquals("8h05 Ecublens VD, EPFL → Chavannes-R., UNIL-Mouline (arr. 9h12)", FormatterFr.formatLeg(l2));

        var l3 = new Journey.Leg.Transport(s1, dT, s4, aT, List.of(), Vehicle.TRAIN, r, h);
        assertEquals("8h05 Lausanne (voie 70) → Chavannes-R., UNIL-Mouline (arr. 9h12)", FormatterFr.formatLeg(l3));
    }

    @Test
    void formatterFrFormatRouteDestinationWorks() {
        var d = LocalDate.of(2025, Month.FEBRUARY, 18);
        var dS = new Stop("A", null, 7, 47);
        var dT = d.atTime(8, 0);
        var aS = new Stop("B", null, 8, 48);
        var aT = d.atTime(9, 0);
        var r = "IC 1";
        var h = "Rorschach";
        var l = new Journey.Leg.Transport(dS, dT, aS, aT, List.of(), Vehicle.TRAIN, r, h);
        assertEquals(r + " Direction " + h, FormatterFr.formatRouteDestination(l));
    }
}