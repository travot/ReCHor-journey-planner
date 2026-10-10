package ch.epfl.rechor.journey;

import ch.epfl.rechor.Preconditions;
import java.util.Objects;

/**
 * Représente un arrêt avec un nom, un nom de quai, ainsi que sa position géographique
 * (longitude et latitude).
 *
 * @param name         le nom de l'arrêt (ne doit pas être {@code null})
 * @param platformName le nom du quai
 * @param longitude    la longitude de l'arrêt (doit être comprise entre -180 et 180)
 * @param latitude     la latitude de l'arrêt (doit être comprise entre -90 et 90)
 *
 * @author Tom (392844)
 */
public record Stop(String name,
     String platformName,
     double longitude,
     double latitude) {

    /**
     * Construit un arrêt avec les informations spécifiées.
     * Vérifie que le nom n'est pas null et que les coordonnées sont valides.
     *
     * @throws NullPointerException     si name est null
     * @throws IllegalArgumentException si longitude n'est pas dans [-180, 180] ou
     *                                  si latitude n'est pas dans [-90, 90]
     */
    public Stop {
        Objects.requireNonNull(name);
        Preconditions.checkArgument(Math.abs(longitude) <= 180);
        Preconditions.checkArgument(Math.abs(latitude) <= 90);
    }
}

