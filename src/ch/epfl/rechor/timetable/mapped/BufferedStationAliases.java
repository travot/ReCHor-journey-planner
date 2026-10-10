package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.StationAliases;

import java.nio.ByteBuffer;
import java.util.List;
/**
 * Classe représentant un tampon binaire structuré, permettant l'accès aux données selon une
 * structure définie.
 * Cette classe assure que les données sont correctement alignées selon la structure fournie.
 *
 * @author Tom (392844)
 */
public final class BufferedStationAliases implements StationAliases {
    private static final int ALIAS_ID = 0; // Offset pour l'index de l'alias
    private static final int STATION_NAME_ID = 1; // Offset pour l'index du nom de station

    private final List<String> stringTable;

    private final StructuredBuffer structureBuffer;

    /**
     * Construit un objet {@code BufferedStationAliases} à partir d'une table de chaînes et
     * d'un tampon binaire.
     *
     * @param stringTable La liste des noms de stations et d'alias.
     * @param buffer Le tampon contenant les indices des alias et des noms de stations.
     */
    public BufferedStationAliases(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        Structure structure = new Structure(
                Structure.field(ALIAS_ID, Structure.FieldType.U16),
                Structure.field(STATION_NAME_ID, Structure.FieldType.U16)
        );
        structureBuffer = new StructuredBuffer(structure, buffer);
    }

    /**
     * Vérifie si l'identifiant est valide.
     *
     * @param id L'identifiant de l'alias.
     * @throws IndexOutOfBoundsException si l'identifiant est hors limites.
     */
    private void checkId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Retourne l'alias associé à l'identifiant donné.
     *
     * @param id L'identifiant de l'alias.
     * @return L'alias correspondant.
     */
    @Override
    public String alias(int id) {
        checkId(id);
        return stringTable.get(structureBuffer.getU16(ALIAS_ID, id));
    }

    /**
     * Retourne le nom de station associé à l'identifiant donné.
     *
     * @param id L'identifiant de l'alias.
     * @return Le nom de la station correspondant à l'alias.
     */
    @Override
    public String stationName(int id) {
        checkId(id);
        return stringTable.get(structureBuffer.getU16(STATION_NAME_ID, id));
    }

    /**
     * Retourne le nombre total d'alias stockés.
     *
     * @return Le nombre d'alias.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}