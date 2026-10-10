package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Représente une liste de connexions (transferts entre arrêts) stockées dans un buffer structuré.
 * Cette classe permet d'accéder aux informations sur les connexions, telles que :
 * - L'arrêt de départ et son heure de départ.
 * - L'arrêt d'arrivée et son heure d'arrivée.
 * - L'identifiant du trajet et sa position dans celui-ci.
 * - La connexion suivante dans l'itinéraire.
 *
 * @author Alban (401009)
 */
public final class BufferedConnections implements Connections {

    private static final int DEP_STOP_ID = 0;
    private static final int DEP_MINUTES = 1;
    private static final int ARR_STOP_ID = 2;
    private static final int ARR_MINUTES = 3;
    private static final int TRIP_POS_ID = 4;

    private final IntBuffer succBuffer;
    private final StructuredBuffer structureBuffer;

    /**
     * Construit une instance de {@code BufferedConnections}.
     *
     * @param buffer      Le tampon binaire contenant les données des connexions.
     * @param succBuffer  Le tampon binaire contenant les indices des connexions suivantes.
     */
    public BufferedConnections(ByteBuffer buffer, ByteBuffer succBuffer) {
        Structure structure = new Structure(
                Structure.field(DEP_STOP_ID, Structure.FieldType.U16),
                Structure.field(DEP_MINUTES, Structure.FieldType.U16),
                Structure.field(ARR_STOP_ID, Structure.FieldType.U16),
                Structure.field(ARR_MINUTES, Structure.FieldType.U16),
                Structure.field(TRIP_POS_ID, Structure.FieldType.S32)
        );
        structureBuffer = new StructuredBuffer(structure, buffer);
        this.succBuffer = succBuffer.asIntBuffer();
    }

    /**
     * Retourne l'identifiant de l'arrêt de départ pour une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant de l'arrêt de départ.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int depStopId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(DEP_STOP_ID, id);
    }

    /**
     * Retourne l'heure de départ en minutes après minuit pour une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return Le nombre de minutes après minuit correspondant à l'heure de départ.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int depMins(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(DEP_MINUTES, id);
    }

    /**
     * Retourne l'identifiant de l'arrêt d'arrivée pour une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant de l'arrêt d'arrivée.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int arrStopId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(ARR_STOP_ID, id);
    }

    /**
     * Retourne l'heure d'arrivée en minutes après minuit pour une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return Le nombre de minutes après minuit correspondant à l'heure d'arrivée.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int arrMins(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(ARR_MINUTES, id);
    }

    /**
     * Retourne l'identifiant du trajet associé à une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return L'identifiant du trajet.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int tripId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Bits32_24_8.unpack24(structureBuffer.getS32(TRIP_POS_ID, id));
    }

    /**
     * Retourne la position du trajet dans l'itinéraire pour une connexion donnée.
     *
     * @param id L'identifiant de la connexion.
     * @return La position du trajet.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int tripPos(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return Bits32_24_8.unpack8(structureBuffer.getS32(TRIP_POS_ID, id));
    }

    /**
     * Retourne l'identifiant de la connexion suivante dans l'itinéraire.
     *
     * @param id L'identifiant de la connexion actuelle.
     * @return L'identifiant de la connexion suivante.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int nextConnectionId(int id) {
        if (id < 0 || id > size()) {
            throw new IndexOutOfBoundsException();
        }
        return succBuffer.get(id);
    }

    /**
     * Retourne le nombre total de connexions stockées.
     *
     * @return Le nombre total de connexions.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}