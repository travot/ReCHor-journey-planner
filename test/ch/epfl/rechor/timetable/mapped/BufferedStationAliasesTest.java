package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Arrays;

public class BufferedStationAliasesTest {

    @Test
    public void testAlias() {
        List<String> stringTable = Arrays.asList("Alias1", "Alias2", "Alias3");

        // Allouer un buffer de 12 octets pour 3 alias et leurs noms de stations
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort(0, (short) 0); // Alias pour index 0
        buffer.putShort(4, (short) 1); // Alias pour index 1
        buffer.putShort(8, (short) 2); // Alias pour index 2

        BufferedStationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        assertEquals("Alias1", stationAliases.alias(0));
        assertEquals("Alias2", stationAliases.alias(1));
        assertEquals("Alias3", stationAliases.alias(2));
    }

    @Test
    public void testStationName() {
        List<String> stringTable = Arrays.asList("Station A", "Station B", "Station C");

        // Allouer un buffer de 12 octets pour 3 alias et leurs noms de stations
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.putShort(2, (short) 0); // Station name index pour alias 0
        buffer.putShort(6, (short) 1); // Station name index pour alias 1
        buffer.putShort(10, (short) 2); // Station name index pour alias 2

        BufferedStationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        // Vérifier les noms de station
        assertEquals("Station A", stationAliases.stationName(0));
        assertEquals("Station B", stationAliases.stationName(1));
        assertEquals("Station C", stationAliases.stationName(2));
    }

    @Test
    public void testOutOfBounds() {
        List<String> stringTable = Arrays.asList("Alias1", "Alias2", "Alias3");
        ByteBuffer buffer = ByteBuffer.allocate(12);

        BufferedStationAliases stationAliases = new BufferedStationAliases(stringTable, buffer);

        // Vérifier que l'accès à un index hors limite lance une exception
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.alias(3));
        assertThrows(IndexOutOfBoundsException.class, () -> stationAliases.stationName(3));
    }
}
