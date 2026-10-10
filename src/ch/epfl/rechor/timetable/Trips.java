package ch.epfl.rechor.timetable;

/**
 * Interface représentant les informations relatives aux trajets.
 * Cette interface hérite de Indexed et fournit des méthodes permettant d'obtenir des informations
 * sur un trajet, telles que l'identifiant de la route associée et la destination du trajet.
 *
 * @author Tom (392844)
 */
public interface Trips extends Indexed {

    /**
     * Retourne l'identifiant de la route associée à un trajet donné.
     *
     * @param id l'identifiant du trajet
     * @return l'identifiant de la route associée
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int routeId(int id);

    /**
     * Retourne la destination du trajet correspondant à un identifiant donné.
     *
     * @param id l'identifiant du trajet
     * @return la destination du trajet
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    String destination(int id);

}

