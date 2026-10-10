package ch.epfl.rechor;

/**
 * Classe utilitaire contenant des méthodes pour vérifier les préconditions.
 * Cette classe ne peut pas être instanciée.
 *
 * @author Alban (401009)
 */
public final class Preconditions {

    /**
     * Constructeur privé pour empêcher l'instanciation de la classe.
     */
    private Preconditions() {}

    /**
     * Vérifie que la condition est vraie. Si ce n'est pas le cas, une exception est levée.
     *
     * @param shouldBeTrue la condition qui doit être vraie
     * @throws IllegalArgumentException si la condition est fausse
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}