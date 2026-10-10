package ch.epfl.rechor.gui;

import ch.epfl.rechor.journey.Vehicle;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Classe non instanciable utilitaire permettant de gérer les icônes associées aux différents types de véhicules.
 * Les images sont chargées à la demande et mises en cache pour réutilisation.
 * @author Alban (401009)
 */
public class VehicleIcons {

    // Map associant un type de véhicule à son icône correspondante.
    private static final Map<Vehicle, Image> maps = new EnumMap<>(Vehicle.class);

    /**
     * Constructeur privé pour empêcher l'instanciation externe.
     */
    private VehicleIcons() {}

    /**
     * Retourne l'icône associée au véhicule donné.
     * Si l'icône n'a pas encore été chargée, elle est automatiquement créée
     * à partir du nom du véhicule et ajoutée au cache.
     *
     * @param vehicle Le véhicule dont on veut obtenir l'icône.
     * @return L'image représentant le véhicule.
     */
    public static Image iconFor(Vehicle vehicle) {
        return maps.computeIfAbsent(vehicle, v -> new Image(v.name() + ".png"));
    }
}