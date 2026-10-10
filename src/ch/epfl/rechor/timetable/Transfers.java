package ch.epfl.rechor.timetable;

/**
 * Interface représentant les informations sur les correspondances entre stations.
 * Cette interface hérite de Indexed et fournit des méthodes permettant d'obtenir des informations
 * relatives aux transferts entre stations, telles que l'identifiant de la station de départ, le
 * temps de correspondance, la station d'arrivée et le temps de trajet entre deux stations.
 *
 * @author Tom (392844)
 */
public interface Transfers extends Indexed {

    /**
     * Retourne l'identifiant de la station de départ correspondant à un identifiant de transfert
     * donné.
     *
     * @param id l'identifiant du transfert
     * @return l'identifiant de la station de départ
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int depStationId(int id);

    /**
     * Retourne le nombre de minutes de correspondance pour un transfert donné.
     *
     * @param id l'identifiant du transfert
     * @return le nombre de minutes de correspondance
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int minutes(int id);

    /**
     * Retourne l'identifiant de la station d'arrivée pour un transfert donné, à partir de
     * l'identifiant de la station de départ.
     *
     * @param stationId l'identifiant de la station de départ
     * @return l'identifiant de la station d'arrivée
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int arrivingAt(int stationId);

    /**
     * Retourne le nombre de minutes entre deux stations données.
     *
     * @param depStationId l'identifiant de la station de départ
     * @param arrStationId l'identifiant de la station d'arrivée
     * @return le nombre de minutes entre les deux stations
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int minutesBetween(int depStationId, int arrStationId);

}

