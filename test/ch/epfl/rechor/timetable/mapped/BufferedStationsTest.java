package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;

public class BufferedStationsTest {

    // Le facteur de conversion à utiliser pour convertir en degrés
    private static final double CONVERSION_FACTOR = 360.0 / Math.pow(2, 32);

    @Test
    public void testName() {
        List<String> stringTable = Arrays.asList("Station1", "Station2", "Station3");
        ByteBuffer buffer = ByteBuffer.allocate(30); // 3 stations, chaque station a 10 octets
        buffer.putShort(0, (short) 0); // Station1
        buffer.putInt(2, 12345678);   // Longitude pour Station1
        buffer.putInt(6, 87654321);   // Latitude pour Station1
        buffer.putShort(10, (short) 1); // Station2
        buffer.putInt(12, 23456789);   // Longitude pour Station2
        buffer.putInt(16, 98765432);   // Latitude pour Station2
        buffer.putShort(20, (short) 2); // Station3
        buffer.putInt(22, 34567890);   // Longitude pour Station3
        buffer.putInt(26, 65432109);   // Latitude pour Station3
        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertEquals("Station1", stations.name(0));
        assertEquals("Station2", stations.name(1));
        assertEquals("Station3", stations.name(2));
    }


    @Test
    public void testLongitudeLatitude() {
        List<String> stringTable = Arrays.asList("Station1", "Station2", "Station3");
        ByteBuffer buffer = ByteBuffer.allocate(30); // 3 stations, chaque station a 10 octets
        buffer.putShort(0, (short) 0); // Station1
        buffer.putInt(2, 12345678);   // Longitude pour Station1
        buffer.putInt(6, 87654321);   // Latitude pour Station1
        buffer.putShort(10, (short) 1); // Station2
        buffer.putInt(12, 23456789);   // Longitude pour Station2
        buffer.putInt(16, 98765432);   // Latitude pour Station2
        buffer.putShort(20, (short) 2); // Station3
        buffer.putInt(22, 34567890);   // Longitude pour Station3
        buffer.putInt(26, 65432109);   // Latitude pour Station3

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        // Application du facteur de conversion pour les tests
        double lonStation1 = 12345678 * CONVERSION_FACTOR;
        double latStation1 = 87654321 * CONVERSION_FACTOR;
        double lonStation2 = 23456789 * CONVERSION_FACTOR;
        double latStation2 = 98765432 * CONVERSION_FACTOR;
        double lonStation3 = 34567890 * CONVERSION_FACTOR;
        double latStation3 = 65432109 * CONVERSION_FACTOR;

        assertEquals(lonStation1, stations.longitude(0), 0.0000001);
        assertEquals(latStation1, stations.latitude(0), 0.0000001);
        assertEquals(lonStation2, stations.longitude(1), 0.0000001);
        assertEquals(latStation2, stations.latitude(1), 0.0000001);
        assertEquals(lonStation3, stations.longitude(2), 0.0000001);
        assertEquals(latStation3, stations.latitude(2), 0.0000001);
    }

    @Test
    public void testOutOfBounds() {
        List<String> stringTable = Arrays.asList("Station1", "Station2", "Station3");
        ByteBuffer buffer = ByteBuffer.allocate(30); // 3 stations, chaque station a 10 octets

        BufferedStations stations = new BufferedStations(stringTable, buffer);

        assertThrows(IndexOutOfBoundsException.class, () -> stations.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.longitude(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stations.latitude(3));
    }
}
