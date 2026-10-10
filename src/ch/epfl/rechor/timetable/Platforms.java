package ch.epfl.rechor.timetable;

/**
 * Interface représentant une collection de plateformes d'une station.
 * Cette interface hérite de Indexed et ajoute des méthodes permettant d'obtenir des informations
 * spécifiques sur une plateforme, telles que son nom et l'identifiant de la station à
 * laquelle elle appartient.
 *
 * @author Tom (392844)
 */
public interface Platforms extends Indexed {

    /**
     * Retourne le nom de la plateforme correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la plateforme
     * @return le nom de la plateforme
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    String name(int id);

    /**
     * Retourne l'identifiant de la station à laquelle appartient la plateforme donnée.
     *
     * @param id l'identifiant de la plateforme
     * @return l'identifiant de la station
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int stationId(int id);

}
