package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDate;
import java.util.Arrays;

import static ch.epfl.rechor.PackedRange.*;
import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Classe permettant de calculer un profil de voyage (optimisé selon plusieurs critères)
 * à partir d'un horaire de transport public (TimeTable).
 * Ce routeur utilise les connexions et transferts du TimeTable pour
 * construire un ensemble de critères optimaux pour les trajets arrivant
 * à une station donnée à une date spécifique.
 *
 * @author Tom (392844) & Alban (401009)
 */
public record Router(TimeTable timeTable) {

    /**
     * Calcule le profil de voyage optimal pour un jour donné
     * en direction d'une station d'arrivée spécifiée.
     * Le profil construit est basé sur une exploration des connexions,
     * intégrant les possibilités de transfert, de continuation sur la même liaison,
     * ou de marche entre stations. Le résultat est encapsulé dans un objet Profile.</p>
     *
     * @param date la date du voyage
     * @param arrStationId l'identifiant de la station d'arrivée
     * @return le profil de voyage calculé pour la date et la station d'arrivée données
     */
    public Profile profile(LocalDate date, int arrStationId) {
        Profile.Builder builder = new Profile.Builder(timeTable(), date, arrStationId);
        Connections connections = timeTable().connectionsFor(date);
        int[] minuteBetween = computeTransferTimes(arrStationId);

        for (int i = 0; i < connections.size(); i++) {
            ParetoFront.Builder frontier = buildFrontier(i, connections, builder, minuteBetween);
            if (frontier.isEmpty()) continue;

            updateTripFrontier(i, connections, builder, frontier);
            updateStationFrontiers(i, connections, builder, frontier);
        }

        return builder.build();
    }

    private int[] computeTransferTimes(int arrStationId) {
        int[] minuteBetween = new int[timeTable.stations().size()];
        Arrays.fill(minuteBetween, -1);
        int inter = timeTable.transfers().arrivingAt(arrStationId);
        for (int i = startInclusive(inter); i < endExclusive(inter); i++) {
            minuteBetween[timeTable.transfers().depStationId(i)] = timeTable().transfers().minutes(i);
        }
        return minuteBetween;
    }

    private ParetoFront.Builder buildFrontier(int connectionId, Connections connections, Profile.Builder builder, int[] minuteBetween) {
        ParetoFront.Builder frontier = new ParetoFront.Builder();

        int arrStopId = connections.arrStopId(connectionId);
        int tripId = connections.tripId(connectionId);

        int arrStationId = timeTable.stationId(arrStopId);

        // Option 1: marcher jusqu'à la station d'arrivée
        if (minuteBetween[arrStationId] >= 0) {
            frontier.add(connections.arrMins(connectionId) + minuteBetween[arrStationId], 0, connectionId);
        }

        // Option 2: continuer avec la même liaison
        ParetoFront.Builder tripBuilder = builder.forTrip(tripId);
        if (tripBuilder != null) frontier.addAll(tripBuilder);

        // Option 3: changer de véhicule à la fin de la liaison
        ParetoFront.Builder stationBuilder = builder.forStation(arrStationId);
        if (stationBuilder != null) {
            stationBuilder.forEach(criteria -> {
                if (PackedCriteria.depMins(criteria) >= connections.arrMins(connectionId)) {
                    frontier.add(PackedCriteria.pack(arrMins(criteria), changes(criteria) + 1, connectionId));
                }
            });
        }

        return frontier;
    }

    private void updateTripFrontier(int connectionId, Connections connections, Profile.Builder builder, ParetoFront.Builder frontier) {
        int tripId = connections.tripId(connectionId);
        ParetoFront.Builder tripBuilder = builder.forTrip(tripId);
        builder.setForTrip(tripId, tripBuilder != null ? tripBuilder.addAll(frontier) : frontier);
    }

    private void updateStationFrontiers(int connectionId, Connections connections, Profile.Builder builder, ParetoFront.Builder frontier) {
        int depStopId = connections.depStopId(connectionId);
        int depStationId = timeTable.stationId(depStopId);

        ParetoFront.Builder stationBuilder = builder.forStation(depStationId);
        if (stationBuilder != null && stationBuilder.fullyDominates(frontier, connections.depMins(connectionId))) {
            return;
        }

        int transferRange = timeTable.transfers().arrivingAt(depStationId);
        for (int j = startInclusive(transferRange); j < endExclusive(transferRange); j++) {
            int d = connections.depMins(connectionId) - timeTable.transfers().minutes(j);
            int transferStationId = timeTable.transfers().depStationId(j);

            ParetoFront.Builder newFrontier = new ParetoFront.Builder();
            frontier.forEach(criteria -> newFrontier.add(PackedCriteria.withPayload(
                    withDepMins(criteria, d),
                    Bits32_24_8.pack(connectionId,
                            connections.tripPos(payload(criteria)) - connections.tripPos(connectionId))
            )));

            ParetoFront.Builder existing = builder.forStation(transferStationId);
            builder.setForStation(transferStationId, existing != null ? existing.addAll(newFrontier) : newFrontier);
        }
    }
}
