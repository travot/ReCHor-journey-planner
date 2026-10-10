package ch.epfl.rechor.journey;

import org.junit.jupiter.api.Test;

import java.util.List;

import static ch.epfl.rechor.journey.Vehicle.*;
import static org.junit.jupiter.api.Assertions.*;

class VehicleTest {
    @Test
    void vehicleEnumIsCorrectlyDefined() {
        var expectedValues = new Vehicle[]{
                TRAM, METRO, TRAIN, BUS, FERRY, AERIAL_LIFT, FUNICULAR
        };
        assertArrayEquals(expectedValues, Vehicle.values());
    }

    @Test
    void vehicleALLIsCorrectlyDefined() {
        var expectedList = List.of(TRAM, METRO, TRAIN, BUS, FERRY, AERIAL_LIFT, FUNICULAR);
        var actualList = ALL;
        assertEquals(expectedList, actualList);
    }
}