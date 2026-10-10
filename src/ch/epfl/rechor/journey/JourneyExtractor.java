package ch.epfl.rechor.journey;

import ch.epfl.rechor.Bits32_24_8;
import ch.epfl.rechor.timetable.Connections;
import ch.epfl.rechor.timetable.TimeTable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Classe utilitaire permettant d'extraire toutes les {@link Journey}
 * possibles à partir d'un {@link Profile} donné, en suivant les critères de dominance
 * (pareto-optimalité) définis dans ce profil.
 * Cette classe est conçue pour fonctionner avec une base de données d'horaires de transport
 * (modélisée via {@link TimeTable}) et pour reconstruire les trajets optimaux
 * (en temps et changements)
 * entre une station de départ et une station d'arrivée, éventuellement avec des correspondances
 * à pied.
 * Cette classe ne peut pas être instanciée.
 * @author Alban (401009)
 */
public class JourneyExtractor {

    private JourneyExtractor() {}

    public static List<Journey> journeys(Profile profile, int depStationId) {
        List<Journey> journeys = new ArrayList<>();
        ParetoFront paretoFront = profile.forStation(depStationId);
        Connections connections = profile.connections();
        TimeTable timeTable = profile.timeTable();

        paretoFront.forEach(packedCriteria -> {
            int depMins = depMins(packedCriteria);
            int finalArrMins = arrMins(packedCriteria);
            int changes = changes(packedCriteria);

            int payload = payload(packedCriteria);
            int connectionId = Bits32_24_8.unpack24(payload);
            int currentNbStops = Bits32_24_8.unpack8(payload);

            List<Journey.Leg> legs = new ArrayList<>();
            int currentConnectionId = connectionId;
            int finalStopId = -1;

            addInitialFootLegIfNeeded(profile, depStationId, connections, timeTable, depMins, legs, connectionId);

            String destination = null;

            for (int i = 0; i <= changes; i++) {
                currentConnectionId = buildTransportLeg(profile, legs, connections, timeTable,
                        currentConnectionId, currentNbStops, destination);

                finalStopId = connections.arrStopId(currentConnectionId);
                destination = profile.trips().destination(connections.tripId(currentConnectionId));

                if (i == changes) break;

                long next = profile.forStation(timeTable.stationId(finalStopId))
                        .get(finalArrMins, changes - i - 1);
                payload = payload(next);
                int nextConnectionId = Bits32_24_8.unpack24(payload);
                int nextDepStopId = connections.depStopId(nextConnectionId);

                addFootTransferLeg(profile, timeTable, connections, legs, currentConnectionId, finalStopId, nextDepStopId);

                currentConnectionId = nextConnectionId;
                currentNbStops = Bits32_24_8.unpack8(payload);
            }

            addFinalFootLegIfNeeded(profile, timeTable, finalStopId, finalArrMins, legs);

            journeys.add(new Journey(legs));
        });

        journeys.sort(Comparator.comparing(Journey::depTime).thenComparing(Journey::arrTime));
        return journeys;
    }

    private static void addInitialFootLegIfNeeded(Profile profile, int depStationId,
                                                  Connections connections, TimeTable timeTable,
                                                  int depMins, List<Journey.Leg> legs, int connectionId) {
        if (timeTable.stationId(connections.depStopId(connectionId)) != depStationId) {
            int initFootStopId = connections.depStopId(connectionId);
            Stop footDep = createStop(timeTable, timeTable.stationId(depStationId), depStationId);
            Stop footArr = createStop(timeTable, timeTable.stationId(initFootStopId), initFootStopId);

            LocalDateTime footDepTime = profile.date().atStartOfDay().plusMinutes(depMins);
            LocalDateTime footArrTime = profile.date().atStartOfDay()
                    .plusMinutes(connections.depMins(connectionId));

            legs.add(new Journey.Leg.Foot(footDep, footDepTime, footArr, footArrTime));
        }
    }

    private static int buildTransportLeg(Profile profile, List<Journey.Leg> legs,
                                         Connections connections, TimeTable timeTable,
                                         int connectionId, int nbIntermediateStops, String destinationHint) {
        int tripId = connections.tripId(connectionId);
        String destination = destinationHint != null ? destinationHint : profile.trips().destination(tripId);
        int depStopId = connections.depStopId(connectionId);
        int depMins = connections.depMins(connectionId);

        List<Journey.Leg.IntermediateStop> stops = new ArrayList<>();
        for (int j = 0; j < nbIntermediateStops; j++) {
            LocalDateTime arrTime = profile.date().atStartOfDay()
                    .plusMinutes(connections.arrMins(connectionId));
            connectionId = connections.nextConnectionId(connectionId);
            int interStopId = connections.depStopId(connectionId);
            Stop interStop = createStop(timeTable, timeTable.stationId(interStopId), interStopId);
            LocalDateTime depTime = profile.date().atStartOfDay()
                    .plusMinutes(connections.depMins(connectionId));
            stops.add(new Journey.Leg.IntermediateStop(interStop, arrTime, depTime));
        }

        int routeId = profile.trips().routeId(tripId);
        String routeName = timeTable.routes().name(routeId);
        Vehicle vehicle = timeTable.routes().vehicle(routeId);
        int arrStopId = connections.arrStopId(connectionId);

        Stop legDep = createStop(timeTable, timeTable.stationId(depStopId), depStopId);
        Stop legArr = createStop(timeTable, timeTable.stationId(arrStopId), arrStopId);
        LocalDateTime legDepTime = profile.date().atStartOfDay().plusMinutes(depMins);
        LocalDateTime legArrTime = profile.date().atStartOfDay()
                .plusMinutes(connections.arrMins(connectionId));

        legs.add(new Journey.Leg.Transport(
                legDep, legDepTime, legArr, legArrTime,
                stops, vehicle, routeName, destination
        ));

        return connectionId;
    }

    private static void addFootTransferLeg(Profile profile, TimeTable timeTable,
                                           Connections connections, List<Journey.Leg> legs,
                                           int prevConnectionId, int stopId1, int stopId2) {
        int stationId1 = timeTable.stationId(stopId1);
        int stationId2 = timeTable.stationId(stopId2);
        int transferMinutes = timeTable.transfers().minutesBetween(stationId1, stationId2);

        LocalDateTime footDepTime = profile.date().atStartOfDay()
                .plusMinutes(connections.arrMins(prevConnectionId));
        LocalDateTime footArrTime = footDepTime.plusMinutes(transferMinutes);

        Stop footDep = createStop(timeTable, stationId1, stopId1);
        Stop footArr = (stopId2 != stopId1)
                ? createStop(timeTable, stationId2, stopId2)
                : footDep;

        legs.add(new Journey.Leg.Foot(footDep, footDepTime, footArr, footArrTime));
    }

    private static void addFinalFootLegIfNeeded(Profile profile, TimeTable timeTable,
                                                int finalStopId, int finalArrMins, List<Journey.Leg> legs) {
        if (timeTable.stationId(finalStopId) != profile.arrStationId()) {
            Stop footDep = createStop(timeTable, timeTable.stationId(finalStopId), finalStopId);
            Stop footArr = createStop(timeTable, profile.arrStationId(), profile.arrStationId());
            LocalDateTime footTime = profile.date().atStartOfDay().plusMinutes(finalArrMins);
            legs.add(new Journey.Leg.Foot(footDep, footTime, footArr, footTime));
        }
    }

    /**
     * Crée un objet {@link Stop} à partir d'une station et d'un arrêt spécifiques.
     * Cette méthode récupère les informations de nom, de quai, de longitude et de latitude
     * à partir de la base de données des stations et arrêts contenue dans le {@link TimeTable}.
     * Elle est utilisée pour construire les étapes d’un trajet dans le système de planification.
     *
     * @param timeTable l'objet {@link TimeTable} contenant les données des arrêts et des stations
     * @param stationId l'identifiant de la station à laquelle appartient l'arrêt
     * @param stopId    l'identifiant spécifique de l'arrêt (plateforme) utilisé dans le trajet
     * @return un objet {@link Stop} représentant l'arrêt avec ses informations détaillées
     */
    private static Stop createStop(TimeTable timeTable, int stationId, int stopId) {
        return new Stop(
                timeTable.stations().name(stationId),
                timeTable.platformName(stopId),
                timeTable.stations().longitude(stationId),
                timeTable.stations().latitude(stationId)
        );
    }
}
