package ch.epfl.rechor.timetable;
/**
 * Interface représentant une collection de stations.
 * Cette interface hérite de Indexed et permet d'obtenir des informations détaillées sur une station
 * en utilisant son identifiant. Les méthodes disponibles permettent de récupérer le nom,
 * la longitude et la latitude d'une station donnée.
 *
 * @author Tom (392844)
 */
public interface Stations extends Indexed {

    /**
     * Retourne le nom de la station correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la station
     * @return le nom de la station
     * @throws IndexOutOfBoundsException si l'id est en dehors des limites de la collection
     */
    String name(int id);

    /**
     * Retourne la longitude de la station correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la station
     * @return la longitude de la station
     * @throws IndexOutOfBoundsException si l'id est en dehors des limites de la collection
     */
    double longitude(int id);

    /**
     * Retourne la latitude de la station correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la station
     * @return la latitude de la station
     * @throws IndexOutOfBoundsException si l'id est en dehors des limites de la collection
     */
    double latitude(int id);

}
