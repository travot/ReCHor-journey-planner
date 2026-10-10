package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.mapped.Structure;
import ch.epfl.rechor.timetable.mapped.StructuredBuffer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

public class StructuredBufferTest {

    @Test
    public void testGetU8() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U8)
        );
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 42);  // Setting the byte value

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);
        assertEquals(42, structuredBuffer.getU8(0, 0));
    }

    @Test
    public void testGetU16() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.U16)
        );
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort((short) 12345);  // Setting the short value

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertEquals(12345, structuredBuffer.getU16(0, 0));
    }

    @Test
    public void testGetS32() {
        Structure structure = new Structure(
                Structure.field(0, Structure.FieldType.S32)
        );
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(987654321);  // Setting the int value

        StructuredBuffer structuredBuffer = new StructuredBuffer(structure, buffer);

        assertEquals(987654321, structuredBuffer.getS32(0, 0));
    }
}
