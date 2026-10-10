package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.journey.Vehicle;
import ch.epfl.rechor.timetable.Routes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Représente une liste de routes stockées dans un buffer structuré.
 * Cette classe permet d'accéder aux informations des routes (nom et type de véhicule)
 * à partir d'une table de chaînes et d'un tampon binaire.
 *
 * @author Alban (401009)
 */
public final class BufferedRoutes implements Routes {
    private static final int NAME_ID = 0;
    private static final int KIND = 1;
    private final List<String> stringTable;

    private final StructuredBuffer structureBuffer;

    /**
     * Construit une instance de {@code BufferedRoutes}.
     *
     * @param stringTable La table contenant les noms des routes.
     * @param buffer      Le tampon binaire contenant les données des routes.
     */
    public BufferedRoutes(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        Structure structure = new Structure(
                Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(KIND, Structure.FieldType.U8)
        );
        structureBuffer = new StructuredBuffer(structure, buffer);
    }

    /**
     * Retourne le type de véhicule utilisé sur une route donnée.
     *
     * @param id L'identifiant de la route.
     * @return Le type de véhicule associé à cette route.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public Vehicle vehicle(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Vehicle.values()[structureBuffer.getU8(KIND, id)];
    }

    /**
     * Retourne le nom de la route correspondant à l'identifiant donné.
     *
     * @param id L'identifiant de la route.
     * @return Le nom de la route.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public String name(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return stringTable.get(structureBuffer.getU16(NAME_ID, id));
    }

    /**
     * Retourne le nombre total de routes disponibles.
     *
     * @return Le nombre de routes stockées.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}