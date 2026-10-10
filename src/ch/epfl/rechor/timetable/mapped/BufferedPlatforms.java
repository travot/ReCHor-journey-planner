package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.Platforms;

import java.nio.ByteBuffer;
import java.util.List;
/**
 * Classe immuable représentant une collection de plateformes stockées dans un tampon de type
 * {@link ByteBuffer}.
 * Elle implémente l'interface {@link Platforms} et permet d'accéder aux noms et aux identifiants
 * des stations.
 *
 * @author Tom (392844)
 */
public final class BufferedPlatforms implements Platforms {

    /** Taille d'un champ dans le tampon (en octets). */
    private static final int FIELD_SIZE = 4;
    /** Offset de l'identifiant du nom dans le tampon. */
    private static final int NAME_ID = 0;
    /** Offset de l'identifiant de la station dans le tampon. */
    private static final int STATION_ID = 2;

    private final List<String> stringTable;
    private final ByteBuffer buffer;

    /**
     * Construit une instance de {@code BufferedPlatforms} avec une table de chaînes et
     * un tampon binaire.
     *
     * @param stringTable La liste des noms associés aux plateformes.
     * @param buffer Le tampon contenant les données des plateformes.
     */
    public BufferedPlatforms(List<String> stringTable, ByteBuffer buffer) {
        this.stringTable = stringTable;
        this.buffer = buffer;
    }

    /**
     * Retourne le nom de la plateforme associée à l'identifiant donné.
     *
     * @param id L'identifiant de la plateforme.
     * @return Le nom de la plateforme.
     * @throws IndexOutOfBoundsException si l'identifiant est hors limites.
     */
    @Override
    public String name(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        int nameIndex = Short.toUnsignedInt(buffer.getShort(id * FIELD_SIZE + NAME_ID));
        return stringTable.get(nameIndex);
    }

    /**
     * Retourne l'identifiant de la station associée à l'identifiant de la plateforme donné.
     *
     * @param id L'identifiant de la plateforme.
     * @return L'identifiant de la station correspondante.
     * @throws IndexOutOfBoundsException si l'identifiant est hors limites.
     */
    @Override
    public int stationId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Short.toUnsignedInt(buffer.getShort(id * FIELD_SIZE + STATION_ID));
    }

    /**
     * Retourne le nombre total de plateformes stockées.
     *
     * @return Le nombre de plateformes.
     */
    @Override
    public int size() {
        return buffer.capacity() / FIELD_SIZE;
    }
}
