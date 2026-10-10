package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente un trajet constitué de plusieurs etapes (legs).
 * @param legs la liste des étapes du trajet (ne doit pas être vide)
 * @author Alban (401009) & Tom (392844)
 */
public record Journey(List<Leg> legs) {

    /**
     * Construit un trajet avec une liste Leg.
     * @throws IllegalArgumentException si :
     *         - la liste est vide
     *         - deux étapes adjacentes sont du même type (transport ou à pied)
     *         - l'heure d'arrivée d'une étape n'est pas avant ou égale à l'heure de départ de
     *         la suivante
     *         - l'arrêt d'arrivée d'une étape est différent de l'arrêt de départ de la suivante
     */
    public Journey {
        Preconditions.checkArgument(!legs.isEmpty());
        for (int i = 1; i < legs.size(); i++) {
            Preconditions.checkArgument(
                    legs.get(i-1) instanceof Leg.Foot!=legs.get(i) instanceof Leg.Foot);
            Preconditions.checkArgument(
                    legs.get(i - 1).arrTime().isBefore(legs.get(i).depTime())
                    || legs.get(i - 1).arrTime().isEqual(legs.get(i).depTime()));
            Preconditions.checkArgument(legs.get(i).depStop().equals(legs.get(i - 1).arrStop()));
        }
        legs = List.copyOf(legs);
    }

    /**
     * Retourne l'arrêt de départ du trajet.
     * @return l'arrêt de départ
     */
    public Stop depStop() {
        return legs.getFirst().depStop();
    }

    /**
     * Retourne l'arrêt d'arrivée du trajet.
     * @return l'arrêt d'arrivée
     */
    public Stop arrStop() {
        return legs.getLast().arrStop();
    }

    /**
     * Retourne l'heure de départ du trajet.
     * @return l'heure de départ
     */
    public LocalDateTime depTime() {
        return legs.getFirst().depTime();
    }

    /**
     * Retourne l'heure d'arrivée du trajet.
     * @return l'heure d'arrivée
     */
    public LocalDateTime arrTime() {
        return legs.getLast().arrTime();
    }

    /**
     * Retourne la durée totale du trajet.
     * @return la durée totale
     */
    public Duration duration() {
        return Duration.between(depTime(), arrTime());
    }

    /**
     * Interface représentant une etape du trajet.
     */
    public sealed interface Leg {
        Stop depStop();
        LocalDateTime depTime();
        Stop arrStop();
        LocalDateTime arrTime();
        List<IntermediateStop> intermediateStops();

        /**
         * Retourne la durée de l'etape.
         * @return la durée de l'etape
         */
        default Duration duration() {
            return Duration.between(depTime(), arrTime());
        }

        /**
         * Représente un arrêt intermédiaire dans une etape de transport.
         * @param stop l'arrêt intermédiaire
         * @param arrTime l'heure d'arrivée à cet arrêt
         * @param depTime l'heure de départ de cet arrêt
         */
        record IntermediateStop(Stop stop,
                                       LocalDateTime arrTime,
                                       LocalDateTime depTime) {

            /**
             * Construit un arrêt intermédiaire.
             *
             * @throws NullPointerException si l'arrets intermédiaire est null
             * @throws IllegalArgumentException si l'heure d'arrivée n'est pas avant ou égale à
             * l'heure de départ
             */
            public IntermediateStop {
                Objects.requireNonNull(stop);
                Preconditions.checkArgument(!depTime.isBefore(arrTime));
            }
        }

        /**
         * Représente une etape de transport avec un véhicule.
         * @param depStop l'arrêt de départ
         * @param depTime l'heure de départ
         * @param arrStop l'arrêt d'arrivée
         * @param arrTime l'heure d'arrivée
         * @param intermediateStops liste des arrêts intermédiaires
         * @param vehicle le type de véhicule utilisé
         * @param route l'itinéraire du véhicule
         * @param destination la destination affichée
         */
        record Transport(Stop depStop,
                                LocalDateTime depTime,
                                Stop arrStop,
                                LocalDateTime arrTime,
                                List<IntermediateStop> intermediateStops,
                                Vehicle vehicle, String route,
                                String destination)
                implements Leg {

            /**
             * Construit une etape du transport.
             * @throws NullPointerException si l'un des paramètres obligatoires est null
             * @throws IllegalArgumentException si l'heure de départ n'est pas avant ou égale
             * à l'heure d'arrivée
             */
            public Transport {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Objects.requireNonNull(vehicle);
                Objects.requireNonNull(route);
                Objects.requireNonNull(destination);
                Preconditions.checkArgument(
                        depTime.isBefore(arrTime) || depTime.isEqual(arrTime));

                intermediateStops = List.copyOf(intermediateStops);
            }

            @Override
            public Stop depStop() {
                return depStop;
            }

            @Override
            public LocalDateTime depTime() {
                return depTime;
            }

            @Override
            public Stop arrStop() {
                return arrStop;
            }

            @Override
            public LocalDateTime arrTime() {
                return arrTime;
            }

            @Override
            public List<IntermediateStop> intermediateStops() {
                return intermediateStops;
            }
        }

        /**
         * Représente une etape du trajet effectué à pied.
         * @param depStop l'arrêt de départ
         * @param depTime l'heure de départ
         * @param arrStop l'arrêt d'arrivée
         * @param arrTime l'heure d'arrivée
         */
        record Foot(Stop depStop,
                           LocalDateTime depTime,
                           Stop arrStop,
                           LocalDateTime arrTime)
                implements Leg {

            /**
             * Construit une etape à pied.
             * @throws NullPointerException si l'un des paramètres obligatoires est null
             * @throws IllegalArgumentException si l'heure de départ n'est pas avant ou égale
             * à l'heure d'arrivée
             */
            public Foot {
                Objects.requireNonNull(depStop);
                Objects.requireNonNull(depTime);
                Objects.requireNonNull(arrStop);
                Objects.requireNonNull(arrTime);
                Preconditions.checkArgument(
                        depTime.isBefore(arrTime) || depTime.isEqual(arrTime));
            }

            @Override
            public List<IntermediateStop> intermediateStops() {
                return new ArrayList<>();
            }

            /**
             * Vérifie si l'etape représente un transfert entre deux véhicules.
             * Un transfert a lieu lorsque l'arrêt de départ et d'arrivée ont le même nom.
             * @return true si c'est un transfert, false sinon
             */
            public boolean isTransfer() {
                return depStop.name().equals(arrStop.name());
            }
        }
    }
}

