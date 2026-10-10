package ch.epfl.rechor;

import java.util.List;
import java.util.Map;

/**
 * Interface représentant une structure de données JSON de manière générique.
 * Les implémentations de cette interface permettent de représenter les différents types
 * de données possibles dans un document JSON : tableaux, objets, chaînes de caractères,
 * et nombres.
 * Cette interface utilise des <code>records</code> Java pour les différentes représentations
 * des éléments JSON, garantissant une structure immuable et concise.
 * @author Tom (392844)
 */
public sealed interface Json permits Json.JArray, Json.JNumber, Json.JObject, Json.JString {

    /**
     * Représente un tableau JSON (type <code>Array</code> en JSON).
     * Cette classe contient une liste d'éléments JSON qui peuvent être de n'importe quel type
     * valide (autres instances de <code>Json</code>).
     */
    record JArray(List<Json> list) implements Json {

        /**
         * Retourne une représentation sous forme de chaîne de caractères de l'array JSON.
         * La méthode concatène chaque élément de la liste en respectant la
         * syntaxe d'un tableau JSON
         * en utilisant des virgules pour séparer les éléments.
         *
         * @return La chaîne de caractères représentant l'array JSON.
         */
        @Override
        public String toString() {
            return list.stream()
                    .map(Object::toString)
                    .reduce("[", (a, b) ->
                            a.equals("[") ? a + b : a + "," + b) + "]";
        }
    }

    /**
     * Représente un objet JSON (type <code>Object</code> en JSON).
     * Cette classe contient une map de paires clé-valeur où la clé est une chaîne de caractères
     * et la valeur peut être n'importe quel type JSON.
     */
    record JObject(Map<String, Json> map) implements Json {

        /**
         * Retourne une représentation sous forme de chaîne de caractères de l'objet JSON.
         * La méthode concatène chaque paire clé-valeur en respectant la syntaxe d'un objet JSON
         * en utilisant des virgules pour séparer les paires.
         * @return La chaîne de caractères représentant l'objet JSON.
         */
        @Override
        public String toString() {
            return map.entrySet().stream()
                    .map(entry -> "\"" + entry.getKey() + "\":"
                            + entry.getValue())
                    .reduce("{", (a, b) -> a.equals("{") ? a + b : a
                            + "," + b) + "}";
        }
    }

    /**
     * Représente une chaîne de caractères JSON (type <code>String</code> en JSON).
     * Cette classe encapsule une chaîne de caractères qui représente une valeur JSON.
     */
    record JString(String stringValue) implements Json {

        /**
         * Retourne une représentation sous forme de chaîne de caractères de la valeur
         * de type chaîne JSON.
         * La méthode ajoute des guillemets autour de la chaîne pour respecter la syntaxe JSON.
         * @return La chaîne de caractères représentant la valeur de type chaîne JSON.
         */
        @Override
        public String toString() {
            return "\"" + stringValue + "\"";
        }
    }

    /**
     * Représente un nombre JSON (type <code>Number</code> en JSON).
     * Cette classe encapsule une valeur numérique qui peut être un entier ou un nombre à
     * virgule flottante.
     */
    record JNumber(double nb) implements Json {

        /**
         * Retourne une représentation sous forme de chaîne de caractères de la valeur de
         * type nombre JSON.
         * La méthode retourne la valeur numérique sous forme de chaîne sans ajout de guillemets.
         * @return La chaîne de caractères représentant la valeur de type nombre JSON.
         */
        @Override
        public String toString() {
            return Double.toString(nb);
        }
    }
}
