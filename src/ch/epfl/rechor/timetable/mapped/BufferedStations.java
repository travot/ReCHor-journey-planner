package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Stations;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Classe représentant un tampon binaire structuré, permettant l'accès aux données selon une
 * structure définie.
 * Cette classe assure que les données sont correctement alignées selon la structure fournie.
 *
 * @author Tom (392844)
 */
public final class BufferedStations implements Stations {

    private static final int NAME_ID = 0;
    private static final int LON = 1;
    private static final int LAT = 2;
    private static final double CONVERSION = Math.scalb(360.0, -32);


    private final List<String> stringTable;
    private final StructuredBuffer structureBuffer;

    /**
     * Construit un objet {@code BufferedStations} avec une table de chaînes et un tampon binaire.
     *
     * @param stringTable La liste des noms de stations.
     * @param buffer Le tampon contenant les données des stations.
     * @throws NullPointerException si l'un des arguments est null.
     * @throws IllegalArgumentException si la taille du tampon n'est pas un multiple de la taille
     * d'un champ.
     */
    public BufferedStations(List<String> stringTable, ByteBuffer buffer) {
        if (stringTable == null || buffer == null) {
            throw new NullPointerException();
        }
        this.stringTable = stringTable;
        Structure structure = new Structure(
                Structure.field(NAME_ID, Structure.FieldType.U16),
                Structure.field(LON, Structure.FieldType.S32),
                Structure.field(LAT, Structure.FieldType.S32)
        );
        structureBuffer = new StructuredBuffer(structure, buffer);
    }

    /**
     * Vérifie si l'identifiant est valide.
     *
     * @param id L'identifiant de la station.
     * @throws IndexOutOfBoundsException si l'identifiant est hors limites.
     */
    private void checkId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Retourne le nom de la station associée à l'identifiant donné.
     *
     * @param id L'identifiant de la station.
     * @return Le nom de la station.
     */
    @Override
    public String name(int id) {
        checkId(id);
        return stringTable.get(structureBuffer.getU16(NAME_ID, id));
    }

    /**
     * Retourne la longitude de la station associée à l'identifiant donné.
     *
     * @param id L'identifiant de la station.
     * @return La longitude de la station en degrés.
     */
    @Override
    public double longitude(int id) {
        checkId(id);
        return structureBuffer.getS32(LON, id) * CONVERSION;
    }

    /**
     * Retourne la latitude de la station associée à l'identifiant donné.
     *
     * @param id L'identifiant de la station.
     * @return La latitude de la station en degrés.
     */
    @Override
    public double latitude(int id) {
        checkId(id);
        return structureBuffer.getS32(LAT, id) * CONVERSION;
    }

    /**
     * Retourne le nombre total de stations stockées.
     *
     * @return Le nombre de stations.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}