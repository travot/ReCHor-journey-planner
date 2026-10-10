package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BufferedRoutesTest {
    private static final List<String> STRING_TABLE_1 =
            List.of("Metro M1", "Tram 3", "Bus 10");
    private final ByteBuffer ROUTES_1 = getByteBuffer();

    static HexFormat  hexFormat  = HexFormat.ofDelimiter( " " );

    private static ByteBuffer getByteBuffer() {
        String hexStations = "00 00 01 00 01 00"; // Two routes: "Metro M1" (METRO) and "Tram 3" (TRAM)
        byte [] bytes = hexFormat.parseHex(hexStations);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return bb;
    }

    @Test
    void bufferedRoutesSizeWorks() {
        var routes = new BufferedRoutes(STRING_TABLE_1, ROUTES_1);

        assertEquals(2, routes.size());
    }

    @Test
    void bufferedRoutesNameThrowsOnInvalidIndex() {
        var routes = new BufferedRoutes(STRING_TABLE_1, ROUTES_1);

        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.name(2));
    }

    @Test
    void bufferedRoutesNameWorks() {
        var routes = new BufferedRoutes(STRING_TABLE_1, ROUTES_1);

        assertEquals("Metro M1", routes.name(0));
        assertEquals("Tram 3", routes.name(1));
    }

    @Test
    void bufferedRoutesVehicleWorks() {
        var routes = new BufferedRoutes(STRING_TABLE_1, ROUTES_1);

        assertEquals(Vehicle.METRO, routes.vehicle(0));
        assertEquals(Vehicle.TRAM, routes.vehicle(1));
    }

    @Test
    void bufferedRoutesVehicleThrowsOnInvalidIndex() {
        var routes = new BufferedRoutes(STRING_TABLE_1, ROUTES_1);

        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> routes.vehicle(2));
    }
}
