package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Trips;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Représente une liste de trajets stockés dans un buffer structuré.
 * Cette classe permet d'accéder aux informations des trajets
 * (destination et identifiant de la route)
 * à partir d'une table de chaînes et d'un tampon binaire.
 *
 * @author Tom (392844)
 */
public final class BufferedTrips implements Trips {
    private static final int ROUTE_ID = 0;
    private static final int DESTINATION_ID = 1;
    private final List<String> stringTable;
    private final StructuredBuffer structureBuffer;

    /**
     * Construit une instance de {@code BufferedTrips}.
     *
     * @param stringTable La table contenant les noms des destinations.
     * @param buffer      Le tampon binaire contenant les données des trajets.
     */
    public BufferedTrips(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        Structure structure = new Structure(
                Structure.field(ROUTE_ID, Structure.FieldType.U16),
                Structure.field(DESTINATION_ID, Structure.FieldType.U16)
        );
        structureBuffer = new StructuredBuffer(structure, buffer);
    }

    /**
     * Retourne la destination associée à un trajet donné.
     *
     * @param id L'identifiant du trajet.
     * @return Le nom de la destination associée à ce trajet.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public String destination(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return stringTable.get(structureBuffer.getU16(DESTINATION_ID, id));
    }

    /**
     * Retourne l'identifiant de la route associée à un trajet donné.
     *
     * @param id L'identifiant du trajet.
     * @return L'identifiant de la route correspondante.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int routeId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(ROUTE_ID, id);
    }

    /**
     * Retourne le nombre total de trajets stockés.
     *
     * @return Le nombre de trajets.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}