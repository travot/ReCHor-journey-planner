package ch.epfl.rechor.journey;

import ch.epfl.rechor.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cette classe non instanciable fournit une méthode pour convertir une liste
 * de trajets (représentés par des arrêts)
 * en un format GeoJSON.
 * Le format GeoJSON est utilisé pour représenter des objets géographiques,
 * tels que des points, des lignes, des polygones, etc.
 * Elle inclut une méthode statique <code>toGeoJson()</code> qui prend une
 * liste d'objets <code>Stop</code>
 * et génère une représentation GeoJSON des trajets comme une collection de coordonnées distinctes.
 *
 * @author Tom (392844)
 */
public final class JourneyGeoJsonConverter {

    private static final double ROUNDING_FACTOR = 100000.0;

    // Constructeur privé pour rendre la classe non instanciable
    private JourneyGeoJsonConverter() {}

    /**
     * Convertit une liste d'arrêts de trajet en une représentation GeoJSON.
     * La méthode crée un objet JSON représentant une collection de caractéristiques géographiques,
     * spécifiquement une ligne (type <code>LineString</code>) avec des coordonnées
     * distinctes de chaque arrêt, en supprimant uniquement les points successifs identiques.
     *
     * @param journey Une liste d'objets <code>Stop</code>, représentant les arrêts du trajet.
     * @return Un objet JSON représentant le GeoJSON du trajet sous la forme d'une collection de
     * caractéristiques.
     */
    public static Json toGeoJson(List<Stop> journey) {
        List<Json> coordinates = new ArrayList<>();

        double[] prev = null;

        for (Stop stop : journey) {
            double lon = round(stop.longitude());
            double lat = round(stop.latitude());
            double[] current = new double[] {lon, lat};

            if (prev == null || lon != prev[0] || lat != prev[1]) {
                coordinates.add(new Json.JArray(List.of(
                        new Json.JNumber(lon),
                        new Json.JNumber(lat)
                )));
                prev = current;
            }
        }

        return new Json.JObject(Map.of(
                "type", new Json.JString("FeatureCollection"),
                "features", new Json.JArray(List.of(
                        new Json.JObject(Map.of(
                                "type", new Json.JString("Feature"),
                                "geometry", new Json.JObject(Map.of(
                                        "type", new Json.JString("LineString"),
                                        "coordinates", new Json.JArray(coordinates)
                                )),
                                "properties", new Json.JObject(Map.of())
                        ))
                ))
        ));
    }

    /**
     * Arrondit la valeur à cinq décimales.
     * Cette méthode est utilisée pour arrondir les coordonnées géographiques des arrêts de trajet.
     * @param value La valeur à arrondir.
     * @return La valeur arrondie à cinq décimales.
     */
    private static double round(double value) {
        return Math.round(value * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }
}
