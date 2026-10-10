package ch.epfl.rechor;

/**
 * Encoder et de décoder des valeurs dans un entier 32 bits.
 * Classe non instanciable.
 * @author Alban (401009)
 */
public final class Bits32_24_8 {

    private Bits32_24_8() {}

    /**
     * Combine une valeur 24 bits et une valeur 8 bits en un entier 32 bits.
     *
     * @param bits24 la valeur 24 bits (doit tenir sur 24 bits,
     *               c'est-à-dire être compris entre 0 et 2^24 - 1)
     * @param bits8 la valeur 8 bits (doit tenir sur 8 bits,
     *              c'est-à-dire être compris entre 0 et 255)
     * @return un entier 32 bits contenant les deux valeurs
     * @throws IllegalArgumentException si {@code bits24} ou {@code bits8} ne tiennent pas
     * sur le nombre de bits attendu
     */
    public static int pack(int bits24, int bits8) {
        Preconditions.checkArgument((bits8 >> 8) == 0);
        Preconditions.checkArgument((bits24 >> 24) == 0);
        return (bits24 << 8) | bits8;
    }

    /**
     * Extrait la composante 24 bits d'un entier 32 bits.
     *
     * @param bits32 l'entier 32 bits contenant une valeur 24 bits et une valeur 8 bits
     * @return la valeur 24 bits (bits 8 à 31)
     */
    public static int unpack24(int bits32) {
        return (bits32 >> 8) & 0xFFFFFF;
    }

    /**
     * Extrait la composante 8 bits d'un entier 32 bits.
     *
     * @param bits32 l'entier 32 bits contenant une valeur 24 bits et une valeur 8 bits
     * @return la valeur 8 bits (bits 0 à 7)
     */
    public static int unpack8(int bits32) {
        return bits32 & 0xFF;
    }

}
