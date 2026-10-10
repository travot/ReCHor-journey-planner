package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.Trips;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Un profil représentant des informations sur les connexions et les voyages pour une date donnée
 * et une gare d'arrivée spécifique, avec une table de frontières de Pareto associée aux gares.
 * @author Tom (392844)
 */
public record Profile(TimeTable timeTable,
                      LocalDate date,
                      int arrStationId,
                      List<ParetoFront> stationFront) {

    /**
     * Constructeur compact garantissant l'immuabilité du profil en copiant la table des frontières
     * de Pareto.
     *
     * @param timeTable   l'horaire correspondant au profil
     * @param date        la date à laquelle correspond le profil
     * @param arrStationId l'index de la gare d'arrivée
     * @param stationFront la liste des frontières de Pareto associées aux gares
     */
    public Profile {
        stationFront = List.copyOf(stationFront);
    }

    /**
     * Retourne les connexions correspondant au profil pour la date donnée.
     *
     * @return les connexions de l'horaire pour la date donnée
     */
    public Connections connections() {
        return timeTable().connectionsFor(date);
    }

    /**
     * Retourne les voyages correspondant au profil pour la date donnée.
     *
     * @return les voyages de l'horaire pour la date donnée
     */
    public Trips trips() {
        return timeTable().tripsFor(date);
    }

    /**
     * Retourne la frontière de Pareto pour la gare d'index donné.
     *
     * @param stationId l'index de la gare
     * @return la frontière de Pareto associée à la gare spécifiée
     * @throws IndexOutOfBoundsException si l'index de la gare est invalide
     */
    public ParetoFront forStation(int stationId) {
        if (stationId < 0 || stationId >= stationFront.size()) {
            throw new IndexOutOfBoundsException();
        }
        return stationFront.get(stationId);
    }

    /**
     * Le bâtisseur pour construire un profil avec les frontières de Pareto associées
     * aux gares et aux voyages.
     */
    public static final class Builder {
        private final TimeTable timeTable;
        private final LocalDate date;
        private final int arrStationId;
        private final ParetoFront.Builder[] stationFrontBuilders;
        private final ParetoFront.Builder[] tripFrontBuilders;

        /**
         * Construit un bâtisseur pour créer un profil pour un horaire donné, une date et
         * une gare d'arrivée.
         *
         * @param timeTable   l'horaire correspondant au profil
         * @param date        la date correspondant au profil
         * @param arrStationId l'index de la gare d'arrivée
         */
        public Builder(TimeTable timeTable, LocalDate date, int arrStationId) {
            this.timeTable = timeTable;
            this.date = date;
            this.arrStationId = arrStationId;
            this.stationFrontBuilders = new ParetoFront.Builder[timeTable.stations().size()];
            this.tripFrontBuilders = new ParetoFront.Builder[timeTable.tripsFor(date).size()];
        }

        /**
         * Retourne le bâtisseur de la frontière de Pareto pour la gare d'index donné.
         *
         * @param stationId l'index de la gare
         * @return le bâtisseur de la frontière de Pareto pour la gare spécifiée
         * @throws IndexOutOfBoundsException si l'index de la gare est invalide
         */
        public ParetoFront.Builder forStation(int stationId) {
            if (stationId < 0 || stationId >= stationFrontBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return stationFrontBuilders[stationId];
        }

        /**
         * Associe un bâtisseur de la frontière de Pareto à la gare d'index donné.
         *
         * @param stationId l'index de la gare
         * @param builder   le bâtisseur de la frontière de Pareto à associer
         * @throws IndexOutOfBoundsException si l'index de la gare est invalide
         */
        public void setForStation(int stationId, ParetoFront.Builder builder) {
            if (stationId < 0 || stationId >= stationFrontBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            stationFrontBuilders[stationId] = builder;
        }

        /**
         * Retourne le bâtisseur de la frontière de Pareto pour le voyage d'index donné.
         *
         * @param tripId l'index du voyage
         * @return le bâtisseur de la frontière de Pareto pour le voyage spécifié
         * @throws IndexOutOfBoundsException si l'index du voyage est invalide
         */
        public ParetoFront.Builder forTrip(int tripId) {
            if (tripId < 0 || tripId >= tripFrontBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            return tripFrontBuilders[tripId];
        }

        /**
         * Associe un bâtisseur de la frontière de Pareto à un voyage d'index donné.
         *
         * @param tripId l'index du voyage
         * @param builder le bâtisseur de la frontière de Pareto à associer
         * @throws IndexOutOfBoundsException si l'index du voyage est invalide
         */
        public void setForTrip(int tripId, ParetoFront.Builder builder) {
            if (tripId < 0 || tripId >= tripFrontBuilders.length) {
                throw new IndexOutOfBoundsException();
            }
            tripFrontBuilders[tripId] = builder;
        }

        /**
         * Construit le profil à partir des frontières de Pareto associées aux gares et aux voyages.
         *
         * @return le profil créé à partir des bâtisseurs
         */
        public Profile build() {
            List<ParetoFront> stationFronts = new ArrayList<>(stationFrontBuilders.length);
            for (ParetoFront.Builder stationFrontBuilder : stationFrontBuilders) {
                if (stationFrontBuilder != null) {
                    stationFronts.add(stationFrontBuilder.build());
                } else {
                    stationFronts.add(ParetoFront.EMPTY);
                }
            }
            return new Profile(timeTable, date, arrStationId, stationFronts);
        }
    }
}
