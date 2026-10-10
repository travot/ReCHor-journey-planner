package ch.epfl.rechor.journey;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;
import static ch.epfl.rechor.journey.PackedCriteria.*;

/**
 * Représente une front de Pareto, stockant des tuples codés sous forme de long.
 * Une front de Pareto maintient un ensemble de solutions non dominées.
 * @author Alban (401009)
 */
public class ParetoFront {

    private final long[] tuples;

    private ParetoFront(long[] tuples) {
        this.tuples = tuples;
    }

    /**
     * Front de Pareto vide.
     */
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);

    /**
     * Retourne la taille de la front de Pareto.
     *
     * @return le nombre de tuples dans la front.
     */
    public int size() {
        return tuples.length;
    }

    /**
     * Retourne le tuple correspondant aux critères spécifiés.
     *
     * @param arrMins le temps d'arrivée en minutes.
     * @param changes le nombre de changements.
     * @return le tuple correspondant.
     * @throws NoSuchElementException si aucun tuple ne correspond.
     */
    public long get(int arrMins, int changes) {
        for (long criteria : tuples) {
            if (PackedCriteria.arrMins(criteria) == arrMins && PackedCriteria.
                    changes(criteria) == changes) {
                return criteria; //there can be at most one such element
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Applique une action à chaque tuple de la front.
     *
     * @param action l'action à appliquer.
     */
    public void forEach(LongConsumer action) {
        for (int i = 0; i < size(); i++) {
            action.accept(tuples[i]);
        }
    }

    /**
     * Retourne une représentation textuelle de la front de Pareto.
     *
     * @return une chaîne représentant la front.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); i++) {
            if (hasDepMins(tuples[i])) {
                sb.append("Depart: ")
                        .append(depMins(tuples[i]))
                        .append(" ");
            }
            sb.append("Arrive: ")
                    .append(arrMins(tuples[i]))
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * Constructeur de la front de Pareto permettant de construire une front progressivement.
     */
    public static class Builder {

        private long[] tuples;
        private int size;

        /**
         * Crée un constructeur de front de Pareto vide.
         */
        public Builder() {
            this.tuples = new long[2];
            this.size = 0;
        }

        /**
         * Crée une copie d'un autre constructeur.
         *
         * @param that le constructeur à copier.
         */
        public Builder(Builder that) {
            this.tuples = Arrays.copyOf(that.tuples, that.tuples.length);
            this.size = that.size;
        }

        /**
         * Vérifie si la front est vide.
         *
         * @return true si la front est vide, sinon false.
         */
        public boolean isEmpty() {
            return size == 0;
        }

        /**
         * Réinitialise le constructeur.
         *
         * @return ce constructeur après réinitialisation.
         */
        public Builder clear() {
            size = 0;
            return this;
        }

        /**
         * Ajoute un tuple à la front.
         * Supprime les tuples dominés et rejette les tuples dominés par un existant.
         *
         * @param packedTuple le tuple encodé.
         * @return ce constructeur après ajout.
         */
        public Builder add(long packedTuple) {
            boolean foundInsertPos = false;
            int pos = 0;
            int writeIndex = 0;
            long mask = 0xFFFFFFFF00000000L;

            for (int i = 0; i < size; i++) {
                if (!foundInsertPos && (packedTuple & mask) < (tuples[i] & mask)) {
                    foundInsertPos = true;
                    pos = writeIndex;
                }

                if (foundInsertPos && dominatesOrIsEqual(packedTuple, tuples[i])) {
                    // Le tuple existant est dominé par le nouveau tuple.
                    continue;
                }

                tuples[writeIndex++] = tuples[i];

                if (!foundInsertPos && dominatesOrIsEqual(tuples[i], packedTuple)) {
                    // Le tuple existant domine le nouveau, donc on ne l'ajoute pas.
                    return this;
                }
            }

            if (size == tuples.length) {
                int newCapacity = tuples.length == 0 ? 2 : tuples.length * 2;
                tuples = Arrays.copyOf(tuples, newCapacity);
            }

            if (!foundInsertPos) {
                pos = writeIndex;
            }

            System.arraycopy(tuples, pos, tuples, pos + 1, writeIndex - pos);
            tuples[pos] = packedTuple;
            size = writeIndex + 1;

            return this;
        }

        /**
         * Ajoute un tuple à partir de ses composants.
         *
         * @param arrMins le temps d'arrivée en minutes.
         * @param changes le nombre de changements.
         * @param payload la charge utile supplémentaire.
         * @return ce constructeur après ajout.
         */
        public Builder add(int arrMins, int changes, int payload) {
            add(pack(arrMins, changes, payload));
            return this;
        }

        /**
         * Ajoute tous les tuples d'un autre constructeur.
         *
         * @param that l'autre constructeur.
         * @return ce constructeur après ajout.
         */
        public Builder addAll(Builder that) {
            for (int i = 0; i < that.size; i++) {
                add(that.tuples[i]);
            }
            return this;
        }

        /**
         * Vérifie si cette front domine complètement une autre front pour un temps de départ donné
         *
         * @param that la front à vérifier.
         * @param depMins le temps de départ.
         * @return true si tous les tuples de {@code that} sont dominés par au moins un tuple
         * de cette front.
         */
        public boolean fullyDominates(Builder that, int depMins) {
            for (int i = 0; i < that.size; i++) {
                long criteria = that.tuples[i];
                if (!hasDepMins(that.tuples[i])) {
                    criteria = withDepMins(that.tuples[i], depMins);
                }
                boolean isDominated = false;
                for (int j = 0; j < size; j++) {
                    if (dominatesOrIsEqual(tuples[j], criteria)) {
                        isDominated = true;
                        break;
                    }
                }
                if (!isDominated) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Applique une action à chaque tuple du constructeur.
         *
         * @param action l'action à appliquer.
         */
        public void forEach(LongConsumer action) {
            for (int i = 0; i < size; i++) {
                action.accept(tuples[i]);
            }
        }

        /**
         * Construit une front de Pareto immuable à partir de ce constructeur.
         *
         * @return une instance de {@link ParetoFront}.
         */
        public ParetoFront build() {
            return new ParetoFront(Arrays.copyOf(tuples, size));
        }

        /**
         * Retourne une représentation textuelle de la front en cours de construction.
         *
         * @return une chaîne représentant la front.
         */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (hasDepMins(tuples[i])) {
                    sb.append("Depart: ")
                            .append(depMins(tuples[i]))
                            .append(" ");
                }
                sb.append("Arrive: ")
                        .append(arrMins(tuples[i]))
                        .append("\n");
            }
            return sb.toString();
        }
    }
}
