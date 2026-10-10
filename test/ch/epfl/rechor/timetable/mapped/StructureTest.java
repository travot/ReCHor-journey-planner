package ch.epfl.rechor.timetable.mapped;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

import static ch.epfl.rechor.timetable.mapped.Structure.field;
import static org.junit.jupiter.api.Assertions.*;

class StructureTest {
    @Test
    void structureFieldConstructorThrowsIfTypeIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new Structure.Field(0, null);
        });
    }

    @Test
    void structureConstructorThrowsIfFieldsAreNotInOrder() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    field(1, Structure.FieldType.U8),
                    field(0, Structure.FieldType.U8));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    field(1, Structure.FieldType.U8));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Structure(
                    field(0, Structure.FieldType.U8),
                    field(1, Structure.FieldType.U8),
                    field(2, Structure.FieldType.U8),
                    field(3, Structure.FieldType.U8),
                    field(0, Structure.FieldType.U8));
        });
    }

    private static Structure.FieldType randomType(Random rng) {
        var allTypes = Structure.FieldType.values();
        return allTypes[rng.nextInt(allTypes.length)];
    }

    private static Structure.Field[] randomFields(Random rng, int fieldCount) {
        return IntStream.range(0, fieldCount)
                .mapToObj(i -> field(i, randomType(rng)))
                .toArray(Structure.Field[]::new);
    }

    private static int typeSize(Structure.FieldType type) {
        return switch (type) {
            case U8 -> 1;
            case U16 -> 2;
            case S32 -> 4;
        };
    }

    @Test
    void structureTotalSizeIsCorrect() {
        var rng = new Random(2025);
        for (var i = 1; i <= 10; i += 1) {
            var fields = randomFields(rng, i);
            var structure = new Structure(fields);
            var expectedTotalSize = Arrays.stream(fields)
                    .mapToInt(f -> typeSize(f.type()))
                    .sum();
            assertEquals(expectedTotalSize, structure.totalSize());
        }
    }

    @Test
    void structureOffsetIsCorrect() {
        var rng = new Random(2025_2);
        for (var i = 1; i <= 10; i += 1) {
            var fields = randomFields(rng, i);
            var structure = new Structure(fields);
            var offsets = IntStream.concat(
                            IntStream.of(0),
                            Arrays.stream(fields).mapToInt(f -> typeSize(f.type())))
                    .toArray();
            Arrays.parallelPrefix(offsets, Integer::sum);
            var totalSize = offsets[offsets.length - 1];

            for (var f = 0; f < fields.length; f += 1) {
                for (var j = 0; j < 10; j += 1) {
                    var e = rng.nextInt(1000);
                    var expectedOffset = offsets[f] + e * totalSize;
                    assertEquals(expectedOffset, structure.offset(f, e));
                }
            }
        }
    }
}