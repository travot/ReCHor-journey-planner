package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BufferedConnectionsTest {

    private static final HexFormat HEX_FORMAT = HexFormat.ofDelimiter(" ");

    public static ByteBuffer getConnectionsByteBuffer() {
        // Hex string representation of 2 liaisons
        String hexConnections = "03 E8 01 FE 03 ED 02 03 00 00 C8 03 " // First connection
                + "03 F2 02 1C 03 FC 02 26 00 00 C9 02"; // Second connection

        // Convert hex string to byte array
        byte[] bytes = HEX_FORMAT.parseHex(hexConnections);

        // Wrap into a ByteBuffer
        return ByteBuffer.wrap(bytes);
    }

    public static ByteBuffer getSuccBuffer() {
        String hexConnections = "00 00 00 01 "   //0 -> 1
                + "00 00 00 00";                //1 -> 0

        byte[] bytes = HEX_FORMAT.parseHex(hexConnections);

        return ByteBuffer.wrap(bytes);
    }

    private final BufferedConnections connections = new BufferedConnections(getConnectionsByteBuffer(), getSuccBuffer());

    @Test
    void sizeTest(){
        assertEquals(2, connections.size());
    }

    @Test
    void basicTest(){

        //depStopID
        assertEquals(1000, connections.depStopId(0));
        assertEquals(1010, connections.depStopId(1));

        //depMins
        assertEquals(510, connections.depMins(0));
        assertEquals(540, connections.depMins(1));

        //arrStopID
        assertEquals(1005, connections.arrStopId(0));
        assertEquals(1020, connections.arrStopId(1));

        //arrTime
        assertEquals(515, connections.arrMins(0));
        assertEquals(550, connections.arrMins(1));

        //tripID
        assertEquals(200, connections.tripId(0));
        assertEquals(201, connections.tripId(1));

        //tripPos
        assertEquals(3, connections.tripPos(0));
        assertEquals(2, connections.tripPos(1));

        //nextCon
        assertEquals(1, connections.nextConnectionId(0));
        assertEquals(0, connections.nextConnectionId(1));
    }

    @Test
    void emptyBufferTest() {
        ByteBuffer emptyBuffer = ByteBuffer.wrap(new byte[0]);
        BufferedConnections emptyConnections = new BufferedConnections(emptyBuffer, getSuccBuffer());

        assertEquals(0, emptyConnections.size());
        assertThrows(IndexOutOfBoundsException.class, () -> emptyConnections.depStopId(0));
    }

    public static ByteBuffer getLargeConnectionsByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(100 * 12); // 100 connections, each 12 bytes

        for (int i = 0; i < 100; i++) {
            buffer.putShort((short) (2000 + i)); // DEP_STOP_ID (varies)
            buffer.putShort((short) (600 + i));  // DEP_MINUTES (starts at 10:00 AM)
            buffer.putShort((short) (2050 + i)); // ARR_STOP_ID (varies)
            buffer.putShort((short) (610 + i));  // ARR_MINUTES
            buffer.putInt((i << 8) | (i % 10));  // TRIP_POS_ID (trip index encoded)
        }

        return ByteBuffer.wrap(buffer.array());
    }

    public static ByteBuffer getLargeSuccBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(100 * 4);

        for (int i = 0; i < 99; i++) {
            buffer.putInt(i + 1); // Each liaison points to the next
        }
        buffer.putInt(0); // Last liaison loops back to 0 (circular)

        return ByteBuffer.wrap(buffer.array());
    }

    private final BufferedConnections largeConnections =
            new BufferedConnections(getLargeConnectionsByteBuffer(), getLargeSuccBuffer());

    @Test
    void largeDatasetSizeTest() {
        assertEquals(100, largeConnections.size());
    }

    @Test
    void largeDatasetDepartureStopIdTest() {
        assertEquals(2000, largeConnections.depStopId(0));
        assertEquals(2099, largeConnections.depStopId(99));
    }

    @Test
    void largeDatasetNextConnectionTest() {
        for (int i = 0; i < 99; i++) {
            assertEquals(i + 1, largeConnections.nextConnectionId(i));
        }
        assertEquals(0, largeConnections.nextConnectionId(99)); // Circular check
    }

    public static ByteBuffer getNonSequentialTripPositionsBuffer() {
        String hexConnections = "03 E8 01 FE 03 ED 02 03 00 00 C8 05 " // Trip Position 5
                + "03 F2 02 1C 03 FC 02 26 00 00 C9 02"; // Trip Position 2
        byte[] bytes = HEX_FORMAT.parseHex(hexConnections);
        return ByteBuffer.wrap(bytes);
    }

    private final BufferedConnections nonSequentialConnections =
            new BufferedConnections(getNonSequentialTripPositionsBuffer(), getSuccBuffer());

    @Test
    void nonSequentialTripPositionTest() {
        assertEquals(5, nonSequentialConnections.tripPos(0));
        assertEquals(2, nonSequentialConnections.tripPos(1));
    }

    public static ByteBuffer getExtremeTimestampBuffer() {
        String hexConnections = "03 E8 FF FF 03 ED 02 03 00 00 C8 03 " // Departure at max (65,535)
                + "03 F2 00 00 03 FC 02 26 00 00 C9 02"; // Departure at min (0)
        byte[] bytes = HEX_FORMAT.parseHex(hexConnections);
        return ByteBuffer.wrap(bytes);
    }

    private final BufferedConnections extremeTimestamps =
            new BufferedConnections(getExtremeTimestampBuffer(), getSuccBuffer());

    @Test
    void extremeTimestampsTest() {
        assertEquals(65535, extremeTimestamps.depMins(0)); // Maximum departure time
        assertEquals(0, extremeTimestamps.depMins(1));     // Minimum departure time
    }

    public static ByteBuffer getRandomSuccBuffer() {
        String hexSuccessors = "00 00 00 02 "  // 0 -> 2 (skips 1)
                + "00 00 00 00 " // 1 -> 0
                + "00 00 00 01"; // 2 -> 1
        byte[] bytes = HEX_FORMAT.parseHex(hexSuccessors);
        return ByteBuffer.wrap(bytes);
    }

    private final BufferedConnections randomizedConnections =
            new BufferedConnections(getConnectionsByteBuffer(), getRandomSuccBuffer());

    @Test
    void randomizedSuccessorsTest() {
        assertEquals(2, randomizedConnections.nextConnectionId(0)); // Liaison 0 skips to 2
        assertEquals(0, randomizedConnections.nextConnectionId(1)); // Liaison 1 loops to 0
        assertEquals(1, randomizedConnections.nextConnectionId(2)); // Liaison 2 points to 1
    }

    @Test
    void boundaryIndexAccessTest() {
        assertThrows(IndexOutOfBoundsException.class, () -> largeConnections.depStopId(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> largeConnections.depStopId(100)); // Outside range
    }

    @Test
    void corruptDataTest() {
        ByteBuffer corruptBuffer = ByteBuffer.allocate(5); // Incomplete data (not a multiple of 12)
        assertThrows(IllegalArgumentException.class, () -> new BufferedConnections(corruptBuffer, getSuccBuffer()));
    }
}