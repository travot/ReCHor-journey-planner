package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BufferedTripsTest {
    private static final HexFormat HEX_FORMAT = HexFormat.ofDelimiter(" ");

    private static final BufferedTrips smallTrips = new BufferedTrips(getStringTable(), getSmallTripsByteBuffer());
    private static final BufferedTrips largeTrips = new BufferedTrips(getStringTable(), getLargeTripsByteBuffer());

    // Creates a small buffer with 2 trips
    public static ByteBuffer getSmallTripsByteBuffer() {
        String hexTrips =
                "00 01 00 02 "  // Trip 0 → Route 1, Destination "London"
                        + "00 03 00 04";  // Trip 1 → Route 3, Destination "Tokyo"

        byte[] bytes = HEX_FORMAT.parseHex(hexTrips);
        return ByteBuffer.wrap(bytes);
    }

    // Creates a large buffer with 100 trips
    public static ByteBuffer getLargeTripsByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(1000 * 4);
        for (int i = 0; i < 1000; i++) {
            buffer.putShort((short) (i % 100)); // ROUTE_ID (cycling between 0 and 99)
            buffer.putShort((short) (i % 50));  // DESTINATION_ID (cycling between 0 and 49)
        }
        return ByteBuffer.wrap(buffer.array());
    }

    public static List<String> getStringTable() {
        return Arrays.asList(
                "Paris", "Berlin", "London", "Tokyo", "New York",
                "Amsterdam", "Dubai", "Sydney", "Rome", "Moscow",
                "Shanghai", "Seoul", "Toronto", "Mexico City", "Mumbai",
                "Hong Kong", "San Francisco", "Barcelona", "Chicago", "Singapore",
                "Los Angeles", "Lisbon", "Oslo", "Vienna", "Dublin",
                "Bangkok", "Helsinki", "Copenhagen", "Stockholm", "Jakarta",
                "Istanbul", "Beijing", "Brussels", "Kuala Lumpur", "Athens",
                "Montreal", "Buenos Aires", "Warsaw", "Madrid", "Rio de Janeiro",
                "Cape Town", "Santiago", "Bangalore", "Prague", "Milan",
                "Taipei", "Bogotá", "Caracas", "Ho Chi Minh City", "Lagos"
        );
    }

    @Test
    void simpleTest(){
        assertEquals(2, smallTrips.size());
        assertEquals("London", smallTrips.destination(0));
        assertEquals(1, smallTrips.routeId(0));
    }

    @Test
    void simpleInvalidArguments(){
        assertThrows(IndexOutOfBoundsException.class, () -> smallTrips.destination(2));
        assertThrows(IndexOutOfBoundsException.class, () -> smallTrips.routeId(2));


        assertThrows(IndexOutOfBoundsException.class, () -> smallTrips.routeId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> smallTrips.routeId(2));
        assertThrows(IndexOutOfBoundsException.class, () -> largeTrips.routeId(1000));
    }

    @Test
    void largeTripsTest(){
        assertEquals(1000, largeTrips.size());

        assertEquals(0, largeTrips.routeId(0));
        assertEquals(50, largeTrips.routeId(50));
        assertEquals(0, largeTrips.routeId(100)); //cycling around

        assertEquals("Paris", largeTrips.destination(0));
        assertEquals("Berlin", largeTrips.destination(1));
        assertEquals("Lagos", largeTrips.destination(49));
    }

    @Test
    void emptyBufferTest() {
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
        BufferedTrips emptyTrips = new BufferedTrips(getStringTable(), emptyBuffer);

        assertEquals(0, emptyTrips.size());
        assertThrows(IndexOutOfBoundsException.class, () -> emptyTrips.routeId(0));
    }

    @Test
    void randomizedAccessTest() {
        for (int i = 0; i < 100; i += 10) {
            assertEquals(i % 8000, largeTrips.routeId(i));
            assertNotNull(largeTrips.destination(i)); // Ensures valid destination
        }
    }

    @Test
    void corruptDataTest() {
        ByteBuffer corruptBuffer = ByteBuffer.allocate(5); // Not a multiple of 4
        assertThrows(IllegalArgumentException.class, () -> new BufferedTrips(getStringTable(), corruptBuffer));
    }

}
