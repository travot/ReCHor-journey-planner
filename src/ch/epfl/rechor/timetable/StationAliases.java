package ch.epfl.rechor.timetable;

/**
 * Interface représentant les alias d'une station.
 * Cette interface hérite de Indexed et fournit des méthodes permettant de récupérer des
 * informations
 * sur les alias d'une station, ainsi que le nom de la station elle-même.
 *
 * @author Tom (392844)
 */
public interface StationAliases extends Indexed {

    /**
     * Retourne l'alias d'une station correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la station
     * @return l'alias associé à la station
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    String alias(int id);

    /**
     * Retourne le nom de la station correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la station
     * @return le nom de la station
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    String stationName(int id);

}
