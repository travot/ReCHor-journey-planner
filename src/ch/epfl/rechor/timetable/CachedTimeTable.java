package ch.epfl.rechor.timetable;

import java.time.LocalDate;

/**
 * Une implémentation de {@link TimeTable} qui met en cache les données
 * dépendantes de la date (trips et connections) pour éviter des rechargements
 * multiples depuis l'horaire sous-jacent.
 * Le cache ne contient les données que pour une seule date à la fois. Lorsqu'une
 * méthode {@code tripsFor} ou {@code connectionsFor} est appelée avec une date
 * différente de celle actuellement en cache, les deux types de données sont
 * rechargés depuis l'horaire sous-jacent.
 *
 * @author Alban (401009)
 */
public final class CachedTimeTable implements TimeTable {

    private final TimeTable timeTable;

    private Trips cachedTrips;
    private Connections cachedConnections;
    private LocalDate cachedDate;

    /**
     * Construit un {@code CachedTimeTable} basé sur l'horaire sous-jacent donné.
     *
     * @param timeTable l'horaire sous-jacent dont les données seront mises en cache
     */
    public CachedTimeTable(TimeTable timeTable) {
        this.timeTable = timeTable;
        this.cachedTrips = null;
        this.cachedConnections = null;
        this.cachedDate = null;
    }

    /**
     * Retourne les {@link Trips} pour la date donnée. Si les données pour cette date
     * sont déjà en cache, elles sont retournées sans rechargement. Sinon, les données
     * sont obtenues depuis l'horaire sous-jacent, et mises en cache.
     * Si la date est différente de celle actuellement en cache, le cache est invalidé
     * et les {@link Trips} ainsi que les {@link Connections} sont rechargés.
     *
     * @param date la date pour laquelle obtenir les {@link Trips}
     * @return les {@link Trips} correspondants à la date donnée
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        if (cachedConnections == null || !date.equals(cachedDate)) {
            reloadCached(date);
        }
        return cachedTrips;
    }

    /**
     * Retourne les {@link Connections} pour la date donnée. Si les données pour cette date
     * sont déjà en cache, elles sont retournées sans rechargement. Sinon, les données
     * sont obtenues depuis l'horaire sous-jacent, et mises en cache.
     * Si la date est différente de celle actuellement en cache, le cache est invalidé
     * et les {@link Trips} ainsi que les {@link Connections} sont rechargés.
     * @param date la date pour laquelle obtenir les {@link Connections}
     * @return les {@link Connections} correspondants à la date donnée
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        if (cachedConnections == null || !date.equals(cachedDate)) {
            reloadCached(date);
        }
        return cachedConnections;
    }

    /**
     * Recharge les données {@link Trips} et {@link Connections} depuis l'horaire
     * sous-jacent pour la date donnée, et met à jour le cache.
     *
     * @param date la nouvelle date à mettre en cache
     */
    private void reloadCached(LocalDate date) {
        cachedTrips = timeTable.tripsFor(date);
        cachedConnections = timeTable.connectionsFor(date);
        cachedDate = date;
    }

    @Override
    public Stations stations() {
        return timeTable.stations();
    }

    @Override
    public StationAliases stationAliases() {
        return timeTable.stationAliases();
    }

    @Override
    public Platforms platforms() {
        return timeTable.platforms();
    }

    @Override
    public Routes routes() {
        return timeTable.routes();
    }

    @Override
    public Transfers transfers() {
        return timeTable.transfers();
    }
}