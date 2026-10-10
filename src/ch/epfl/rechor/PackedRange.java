package ch.epfl.rechor;

import static ch.epfl.rechor.Bits32_24_8.unpack24;
import static ch.epfl.rechor.Bits32_24_8.unpack8;

/**
 * Classe permettant d'encoder et de manipuler une plage d'entiers
 * dans un seul entier 32 bits.
 * Cette classe est immuable et ne peut pas être instanciée.
 * @author Alban (401009)
 */
public final class PackedRange {

    private PackedRange() {}

    /**
     * Encode une plage d'entiers sous la forme d'un entier 32 bits.
     *
     * @param startInclusive la borne inférieure incluse de la plage
     *                       (0 ≤ startInclusive < 2^24)
     * @param endExclusive la borne supérieure exclue de la plage
     *                     (0 ≤ endExclusive - startInclusive < 2^8)
     * @return un entier 32 bits contenant la plage encodée.
     * @throws IllegalArgumentException si startInclusive ou endExclusive ne respectent pas
     * les contraintes de taille.
     */
    public static int pack(int startInclusive, int endExclusive) {
        Preconditions.checkArgument((startInclusive >> 24) == 0);
        Preconditions.checkArgument((endExclusive - startInclusive >> 8) == 0);

        return Bits32_24_8.pack(startInclusive, endExclusive - startInclusive);
    }

    /**
     * Retourne la longueur de la plage encodée.
     *
     * @param interval la plage encodée sous la forme d'un entier 32 bits.
     * @return la longueur de la plage.
     */
    public static int length(int interval) {
        return unpack8(interval);
    }

    /**
     * Retourne la borne inférieure (incluse) de la plage encodée.
     *
     * @param interval la plage encodée sous la forme d'un entier 32 bits.
     * @return la borne inférieure (incluse) de la plage.
     */
    public static int startInclusive(int interval) {
        return unpack24(interval);
    }

    /**
     * Retourne la borne supérieure (exclue) de la plage encodée.
     *
     * @param interval la plage encodée sous la forme d'un entier 32 bits.
     * @return la borne supérieure (exclue) de la plage.
     */
    public static int endExclusive(int interval) {
        return startInclusive(interval) + length(interval);
    }
}