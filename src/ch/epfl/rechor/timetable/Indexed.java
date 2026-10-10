package ch.epfl.rechor.timetable;

/**
 * Interface représentant une collection indexée.
 * Cette interface définit les opérations de base sur une collection permettant d'obtenir la taille
 * de la collection. Elle est utilisée pour les objets qui peuvent être indexés.
 *
 * @author Tom (392844)
 */
public interface Indexed {

    /**
     * Retourne la taille de la collection.
     *
     * @return la taille de la collection
     */
    int size();

}
