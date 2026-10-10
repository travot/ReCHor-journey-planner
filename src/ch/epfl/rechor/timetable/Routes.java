package ch.epfl.rechor.timetable;

import ch.epfl.rechor.journey.Vehicle;

/**
 * Interface représentant une collection de routes.
 * Cette interface hérite de Indexed et ajoute des méthodes permettant d'obtenir des informations
 * sur une route, telles que le véhicule utilisé pour cette route et le nom de la route elle-même.
 *
 * @author Tom (392844)
 */
public interface Routes extends Indexed {

    /**
     * Retourne le véhicule utilisé pour la route correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la route
     * @return le véhicule associé à la route
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    Vehicle vehicle(int id);

    /**
     * Retourne le nom de la route correspondant à un identifiant donné.
     *
     * @param id l'identifiant de la route
     * @return le nom de la route
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    String name(int id);

}

