package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Interface représentant un horaire de transport.
 * Cette interface fournit des méthodes pour accéder aux différentes informations liées à l'horaire
 * de transport,telles que les stations, les alias de stations, les plateformes, les routes,
 * les transferts et les trajets. Elle propose également des méthodes par défaut pour déterminer si
 * un identifiant d'arrêt correspond à une gare ou une plateforme, ainsi que pour obtenir
 * l'identifiant de la gare ou le nom de la plateforme associé à un arrêt.
 *
 * @author Tom (392844)
 */
public interface TimeTable {

    /**
     * Retourne la collection de stations associées à l'horaire.
     *
     * @return les stations
     */
    Stations stations();

    /**
     * Retourne la collection des alias de stations associée à l'horaire.
     *
     * @return les alias de stations
     */
    StationAliases stationAliases();

    /**
     * Retourne la collection des plateformes associées à l'horaire.
     *
     * @return les plateformes
     */
    Platforms platforms();

    /**
     * Retourne la collection des routes associées à l'horaire.
     *
     * @return les routes
     */
    Routes routes();

    /**
     * Retourne la collection des transferts associée à l'horaire.
     *
     * @return les transferts
     */
    Transfers transfers();

    /**
     * Retourne la collection des trajets pour une date donnée.
     *
     * @param date la date des trajets
     * @return les trajets pour la date spécifiée
     */
    Trips tripsFor(LocalDate date);

    /**
     * Retourne la collection des connexions pour une date donnée.
     *
     * @param date la date des connexions
     * @return les connexions pour la date spécifiée
     */
    Connections connectionsFor(LocalDate date);

    /**
     * Vérifie si l'identifiant d'arrêt donné correspond à une gare.
     * Cette méthode retourne vrai si et seulement si l'index d'arrêt donné est un index de gare,
     * et non un index de voie ou de quai.
     *
     * @param stopId l'identifiant de l'arrêt
     * @return vrai si l'identifiant correspond à une gare, sinon faux
     */
    default boolean isStationId(int stopId) {
        return stopId < stations().size();
    }

    /**
     * Vérifie si l'identifiant d'arrêt donné correspond à une plateforme.
     * Cette méthode retourne vrai si et seulement si l'index d'arrêt donné est un index de voie ou
     * de quai, et non un index de gare.
     *
     * @param stopId l'identifiant de l'arrêt
     * @return vrai si l'identifiant correspond à un quai, sinon faux
     */
    default boolean isPlatformId(int stopId) {
        return stopId >= stations().size();
    }

    /**
     * Retourne l'identifiant de la gare associé à un arrêt donné.
     * Cette méthode retourne l'identifiant de la gare de l'arrêt spécifié, ou l'identifiant de la
     * gare elle-même si l'arrêt correspond à une gare.
     *
     * @param stopId l'identifiant de l'arrêt
     * @return l'identifiant de la gare associée à l'arrêt
     */
    default int stationId(int stopId) {
        return isStationId(stopId) ? stopId : platforms().stationId(stopId - stations().size());
    }

    /**
     * Retourne le nom de la plateforme associée à un arrêt donné.
     * Cette méthode retourne le nom de la voie ou du quai associé à l'arrêt spécifié, ou null si
     * cet arrêt est une gare.
     *
     * @param stopId l'identifiant de l'arrêt
     * @return le nom de la plateforme associée, ou null si l'arrêt est une gare
     */
    default String platformName(int stopId) {
        return isPlatformId(stopId) ? platforms().name(stopId - stations().size()) : null;
    }
}
