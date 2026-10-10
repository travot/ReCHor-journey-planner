package ch.epfl.rechor.timetable;

/**
 * Interface représentant un ensemble de liaisons indexées.
 * Chaque liaison est identifiée par un identifiant unique et
 * possède des informations sur les arrêts de départ et d'arrivée,
 * les heures correspondantes, l'identifiant du trajet, la position
 * dans le trajet et l'identifiant de la prochaine liaison.
 *
 * @author Tom (392844)
 */
public interface Connections extends Indexed {

    /**
     * Retourne l'identifiant de l'arrêt de départ de la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return l'identifiant de l'arrêt de départ
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int depStopId(int id);

    /**
     * Retourne le temps de départ en minutes depuis minuit de la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return le temps de départ en minutes depuis minuit
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int depMins(int id);

    /**
     * Retourne l'identifiant de l'arrêt d'arrivée de la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return l'identifiant de l'arrêt d'arrivée
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int arrStopId(int id);

    /**
     * Retourne le temps d'arrivée en minutes depuis minuit de la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return le temps d'arrivée en minutes depuis minuit
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int arrMins(int id);

    /**
     * Retourne l'identifiant du trajet auquel appartient la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return l'identifiant du trajet
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int tripId(int id);

    /**
     * Retourne la position de la liaison spécifiée dans le trajet.
     *
     * @param id l'identifiant de la liaison
     * @return la position de la liaison dans le trajet
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int tripPos(int id);

    /**
     * Retourne l'identifiant de la prochaine liaison suivant la liaison spécifiée.
     *
     * @param id l'identifiant de la liaison
     * @return l'identifiant de la prochaine liaison
     * @throws IndexOutOfBoundsException si l'index qu'on leur passe est invalide
     */
    int nextConnectionId(int id);
}

