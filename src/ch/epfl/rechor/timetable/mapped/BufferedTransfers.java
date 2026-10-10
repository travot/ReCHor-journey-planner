package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.PackedRange;
import ch.epfl.rechor.timetable.Transfers;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

/**
 * Représente une liste de transferts stockés dans un buffer structuré.
 * Cette classe permet d'accéder aux informations sur les transferts entre stations,
 * incluant les stations de départ et d'arrivée ainsi que le temps de transfert en minutes.
 *
 * @author Alban (401009)
 */
public final class BufferedTransfers implements Transfers {
    private static final int DEP_STATION_ID = 0;
    private static final int ARR_STATION_ID = 1;
    private static final int TRANSFER_MINUTES = 2;

    private final StructuredBuffer structureBuffer;
    private final int[] arrivalStation;

    /**
     * Construit une instance de {@code BufferedTransfers}.
     *
     * @param buffer Le tampon binaire contenant les données des transferts.
     */
    public BufferedTransfers(ByteBuffer buffer) {
        Structure structure = new Structure(
                Structure.field(DEP_STATION_ID, Structure.FieldType.U16),
                Structure.field(ARR_STATION_ID, Structure.FieldType.U16),
                Structure.field(TRANSFER_MINUTES, Structure.FieldType.U8)
        );
        this.structureBuffer = new StructuredBuffer(structure, buffer);

        int maxId = 0 ;
        for (int i = 0; i < size(); i++) {
            int id = depStationId(i);
            if(id > maxId) maxId = id;
        }
        this.arrivalStation = new int[maxId + 1];

        int currentStart = 0;
        while (currentStart < size()) {
            int currentEnd = currentStart + 1;
            int currentStationId = structureBuffer.getU16(ARR_STATION_ID, currentStart);
            while (currentEnd < size() && structureBuffer.
                    getU16(ARR_STATION_ID, currentEnd) == currentStationId) {
                currentEnd++;
            }
            arrivalStation[currentStationId] = PackedRange.pack(currentStart, currentEnd);
            currentStart = currentEnd;
        }
    }

    /**
     * Retourne l'identifiant de la station de départ pour un transfert donné.
     *
     * @param id L'identifiant du transfert.
     * @return L'identifiant de la station de départ.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int depStationId(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU16(DEP_STATION_ID, id);
    }

    /**
     * Retourne le temps de transfert en minutes pour un transfert donné.
     *
     * @param id L'identifiant du transfert.
     * @return Le temps de transfert en minutes.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int minutes(int id) {
        if (id < 0 || id >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return structureBuffer.getU8(TRANSFER_MINUTES, id);
    }

    /**
     * Retourne la station d'arrivée associée à une station donnée.
     *
     * @param stationId L'identifiant de la station de départ.
     * @return L'identifiant de la station d'arrivée correspondante.
     * @throws IndexOutOfBoundsException si l'identifiant est invalide.
     */
    @Override
    public int arrivingAt(int stationId) {
        if (stationId < 0 || stationId >= arrivalStation.length) {
            throw new IndexOutOfBoundsException();
        }
        return arrivalStation[stationId];
    }

    /**
     * Retourne le temps de transfert entre deux stations spécifiques.
     *
     * @param depStationId L'identifiant de la station de départ.
     * @param arrStationId L'identifiant de la station d'arrivée.
     * @return Le temps de transfert en minutes entre les deux stations, ou -1 si aucun
     * transfert direct n'existe.
     * @throws IndexOutOfBoundsException si l'un des identifiants est invalide.
     * @throws NoSuchElementException si aucun temps n'es trouvé.
     */
    @Override
    public int minutesBetween(int depStationId, int arrStationId) {
        if (depStationId < 0 || depStationId >= size() || arrStationId < 0 ||
                arrStationId >= size()) {
            throw new IndexOutOfBoundsException();
        }

        int range = arrivingAt(arrStationId);
        int startIdx = PackedRange.startInclusive(range);
        int endIdx = PackedRange.endExclusive(range);

        for (int i = startIdx; i < endIdx; i++) {
            if (depStationId(i) == depStationId) {
                return minutes(i);
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * Retourne le nombre total de transferts stockés.
     *
     * @return Le nombre de transferts.
     */
    @Override
    public int size() {
        return structureBuffer.size();
    }
}