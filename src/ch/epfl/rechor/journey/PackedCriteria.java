package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;

/**
 * Classe permettant d'encoder et manipuler un ensemble de critères
 * de voyage (heures, changements, payload) dans une seule valeur de type {@code long}.
 * Le format de l'encodage:
 *     Bits 63 à 51 : heure de départ (optionnelle).
 *     Bits 50 à 39 : heure d'arrivée.
 *     Bits 38 à 32 : nombre de changements.
 *     Bits 31 à 0 : payload arbitraire.
 * Cette classe est immuable et ne peut pas être instanciée.
 * @author Alban (401009)
 */
public final class PackedCriteria {

    private PackedCriteria() {}

    // Constantes de décalage
    private static final int SHIFT_DEP = 51;
    private static final int SHIFT_ARR = 39;
    private static final int SHIFT_CHANGES = 32;

    // Constantes de masques
    private static final int MASK_DEP = 0xFFF;
    private static final int MASK_ARR = 0xFFF;
    private static final int MASK_CHANGES = 0x7F;
    private static final long MASK_PAYLOAD = 0xFFFFFFFFL;

    private static final int MINUTES_OFFSET = 240;
    private static final int MAX_DEP = 4095;

    /**
     * Encode un ensemble de critères dans une seule valeur {@code long}.
     *
     * @param arrMins Heure d'arrivée (en minutes après minuit), comprise entre 1 et 2879.
     * @param changes Nombre de changements, entre 0 et 127.
     * @param payload Une valeur additionnelle (32 bits) associée au critère.
     * @return une valeur {@code long} contenant les critères encodés.
     * @throws IllegalArgumentException si les valeurs sont hors limites.
     */
    public static long pack(int arrMins, int changes, int payload) {
        Preconditions.checkArgument(arrMins >= -MINUTES_OFFSET && arrMins < 2880);
        Preconditions.checkArgument(changes >= 0 && changes <= MASK_CHANGES);

        return ((long) transformMinutes(arrMins) << SHIFT_ARR)
                | ((long) changes << SHIFT_CHANGES)
                | (payload & MASK_PAYLOAD);
    }

    /**
     * Vérifie si une heure de départ est présente dans le critère.
     * @param criteria Critère encodé.
     * @return {@code true} si une heure de départ est présente, {@code false} sinon.
     */
    public static boolean hasDepMins(long criteria) {
        return ((criteria >> SHIFT_DEP) & MASK_DEP) != 0;
    }

    /**
     * Retourne l'heure de départ (en minutes après minuit).
     * @param criteria Critère encodé.
     * @return L'heure de départ en minutes.
     * @throws IllegalArgumentException si aucune heure de départ n'est présente.
     */
    public static int depMins(long criteria) {
        Preconditions.checkArgument(hasDepMins(criteria));
        return MAX_DEP - (int) ((criteria >> SHIFT_DEP) & MASK_DEP) - MINUTES_OFFSET;
    }

    /**
     * Retourne l'heure d'arrivée (en minutes après minuit).
     * @param criteria Critère encodé.
     * @return L'heure d'arrivée en minutes.
     */
    public static int arrMins(long criteria) {
        return (int) ((criteria >> SHIFT_ARR) & MASK_ARR) - MINUTES_OFFSET;
    }

    /**
     * Retourne le nombre de changements.
     * @param criteria Critère encodé.
     * @return Le nombre de changements.
     */
    public static int changes(long criteria) {
        return (int) ((criteria >> SHIFT_CHANGES) & MASK_CHANGES);
    }

    /**
     * Retourne la valeur de payload stockée dans les 32 bits de poids faible.
     * @param criteria Critère encodé.
     * @return Le payload.
     */
    public static int payload(long criteria) {
        return (int) criteria;
    }

    /**
     * Vérifie si un critère est aussi bon ou meilleur qu'un autre (pareto dominance).
     * @param criteria1 Premier critère.
     * @param criteria2 Deuxième critère.
     * @return {@code true} si le premier critère domine ou est égal au second.
     * @throws IllegalArgumentException si l'un des crirtere ne possede pas de depMins
     * par rapport a l'autre
     */
    public static boolean dominatesOrIsEqual(long criteria1, long criteria2) {
        Preconditions.checkArgument(hasDepMins(criteria1) == hasDepMins(criteria2));
        if (hasDepMins(criteria1)) {
            return depMins(criteria1) >= depMins(criteria2)
                    && arrMins(criteria1) <= arrMins(criteria2)
                    && changes(criteria1) <= changes(criteria2);
        }
        return arrMins(criteria1) <= arrMins(criteria2)
                && changes(criteria1) <= changes(criteria2);
    }

    /**
     * Retourne une version du critère sans heure de départ.
     * @param criteria Critère encodé.
     * @return Une copie du critère sans l'heure de départ.
     */
    public static long withoutDepMins(long criteria) {
        return criteria & 0x0007FFFFFFFFFFFFL;
    }

    /**
     * Retourne une version du critère avec une nouvelle heure de départ.
     * @param criteria Critère original.
     * @param depMins1 Nouvelle heure de départ en minutes.
     * @return Le critère mis à jour.
     */
    public static long withDepMins(long criteria, int depMins1) {
        return withoutDepMins(criteria)
                | ((long) (MAX_DEP - transformMinutes(depMins1)) << SHIFT_DEP);
    }

    /**
     * Retourne une version du critère avec un changement supplémentaire.
     * @param criteria Critère original.
     * @return Le critère mis à jour.
     */
    public static long withAdditionalChange(long criteria) {
        return (criteria & ((-1L << SHIFT_ARR) | ((1L << SHIFT_CHANGES) - 1)))
                | ((long) (changes(criteria) + 1) << SHIFT_CHANGES);
    }

    /**
     * Retourne une version du critère avec un nouveau payload.
     * @param criteria Critère original.
     * @param payload1 Nouveau payload.
     * @return Le critère mis à jour.
     */
    public static long withPayload(long criteria, int payload1) {
        return (criteria & 0xFFFFFFFF00000000L) | (payload1 & MASK_PAYLOAD);
    }

    // Méthode auxiliaire pour transformer les minutes en tenant compte du décalage
    private static int transformMinutes(int minutes) {
        return minutes + MINUTES_OFFSET;
    }
}
