package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BufferedPlatformsTest {

    @Test
    public void testName() {
        List<String> stringTable = Arrays.asList("Platform A", "Platform B", "Platform C");

        // Allouer un buffer de 12 octets pour 3 plateformes, chaque plateforme prenant 4 octets (2 octets pour l'index et 2 pour l'ID)
        ByteBuffer buffer = ByteBuffer.allocate(12);

        // Remplir le buffer avec les indices des plateformes et les IDs
        buffer.putShort(0, (short) 0); // Platform A index
        buffer.putShort(4, (short) 1); // Platform B index
        buffer.putShort(8, (short) 2); // Platform C index
        buffer.putShort(2, (short) 101); // Station ID pour Platform A
        buffer.putShort(6, (short) 102); // Station ID pour Platform B
        buffer.putShort(10, (short) 103); // Station ID pour Platform C

        // Créer un objet BufferedPlatforms avec la table des chaînes et le buffer
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        // Vérifier les noms des plateformes
        assertEquals("Platform A", platforms.name(0));
        assertEquals("Platform B", platforms.name(1));
        assertEquals("Platform C", platforms.name(2));
    }

    @Test
    public void testStationId() {
        List<String> stringTable = Arrays.asList("Platform A", "Platform B", "Platform C");

        // Allouer un buffer de 12 octets pour 3 plateformes, chaque plateforme prenant 4 octets (2 octets pour l'index et 2 pour l'ID)
        ByteBuffer buffer = ByteBuffer.allocate(12);

        buffer.putShort(0, (short) 0); // Platform A index
        buffer.putShort(4, (short) 1); // Platform B index
        buffer.putShort(8, (short) 2); // Platform C index
        buffer.putShort(2, (short) 101); // Station ID pour Platform A
        buffer.putShort(6, (short) 102); // Station ID pour Platform B
        buffer.putShort(10, (short) 103); // Station ID pour Platform C

        // Créer un objet BufferedPlatforms avec la table des chaînes et le buffer
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        // Vérifier les IDs des stations
        assertEquals(101, platforms.stationId(0));
        assertEquals(102, platforms.stationId(1));
        assertEquals(103, platforms.stationId(2));
    }

    @Test
    public void testOutOfBounds() {
        List<String> stringTable = Arrays.asList("Platform A", "Platform B", "Platform C");

        // Allouer un buffer de 12 octets pour 3 plateformes
        ByteBuffer buffer = ByteBuffer.allocate(12);

        // Créer un objet BufferedPlatforms avec la table des chaînes et le buffer
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, buffer);

        // Vérifier que l'accès à un index hors limite lance une exception
        assertThrows(IndexOutOfBoundsException.class, () -> platforms.name(3));
        assertThrows(IndexOutOfBoundsException.class, () -> platforms.stationId(3));
    }
}
