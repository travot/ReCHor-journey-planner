package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ICalConverterTest {

    @Test
    void testToIcalendar() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 2, 25, 16, 13);

        Stop epfl = new Stop("EPFL", "Ecublens VD", 46.5191, 6.5668);
        Stop renens = new Stop("Renens VD, gare", "1", 46.5333, 6.5794);
        Stop lausanne = new Stop("Lausanne", "Lausanne", 46.5160, 6.6328);
        Stop romont = new Stop("Romont FR", "Romont FR", 46.6960, 6.9166);
        Stop bulle = new Stop("Bulle", "Bulle", 46.6167, 7.0500);
        Stop gruyeres = new Stop("Gruyères", "Gruyères", 46.5833, 7.0833);

        Journey.Leg.Foot footLeg1 = new Journey.Leg.Foot(epfl, baseTime, renens, baseTime.plusMinutes(6));
        Journey.Leg.Transport train1 = new Journey.Leg.Transport(renens, baseTime.plusMinutes(13), lausanne, baseTime.plusMinutes(20), List.of(), Vehicle.TRAIN, "SBB", "IC1");
        Journey.Leg.Foot footLeg2 = new Journey.Leg.Foot(lausanne, baseTime.plusMinutes(20), lausanne, baseTime.plusMinutes(25));
        Journey.Leg.Transport train2 = new Journey.Leg.Transport(lausanne, baseTime.plusMinutes(27), romont, baseTime.plusMinutes(60), List.of(), Vehicle.TRAIN, "SBB", "R2");
        Journey.Leg.Foot footLeg3 = new Journey.Leg.Foot(romont, baseTime.plusMinutes(60), romont, baseTime.plusMinutes(63));
        Journey.Leg.Transport train3 = new Journey.Leg.Transport(romont, baseTime.plusMinutes(69), bulle, baseTime.plusMinutes(88), List.of(), Vehicle.TRAIN, "TPF", "R3");
        Journey.Leg.Foot footLeg4 = new Journey.Leg.Foot(bulle, baseTime.plusMinutes(88), bulle, baseTime.plusMinutes(91));
        Journey.Leg.Transport train4 = new Journey.Leg.Transport(bulle, baseTime.plusMinutes(97), gruyeres, baseTime.plusMinutes(104), List.of(), Vehicle.TRAIN, "TPF", "R4");

        Journey journey = new Journey(List.of(footLeg1, train1, footLeg2, train2, footLeg3, train3, footLeg4, train4));

        String icalendar = JourneyIcalConverter.toIcalendar(journey);

        assertNotNull(icalendar);
        assertTrue(icalendar.contains("BEGIN:VCALENDAR"));
        assertTrue(icalendar.contains("BEGIN:VEVENT"));
        System.out.println(icalendar);
    }
}