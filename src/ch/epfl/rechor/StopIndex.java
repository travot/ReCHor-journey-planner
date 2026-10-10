package ch.epfl.rechor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Une classe immuable représentant un index de noms d'arrêts, avec recherche
 * insensible à la casse et aux accents, et prise en charge des alias.
 * Elle permet de retrouver les noms d'arrêts principaux correspondant à une requête
 * donnée, en tenant compte des équivalences de caractères accentués et de la casse.
 * @author Alban (401009)
 */
public final class StopIndex {
    private final List<String> canonicalStops;
    private final Map<String, String> aliasToCanonical;

    /**
     * Construit un StopIndex avec les noms d'arrêts principaux et une table d'alias.
     *
     * @param canonicalStops Liste des noms d'arrêts principaux
     * @param aliasToCanonical Map des alias vers leur nom principal (canonique)
     */
    public StopIndex(List<String> canonicalStops, Map<String, String> aliasToCanonical) {
        this.canonicalStops = new ArrayList<>(canonicalStops);
        this.aliasToCanonical = new HashMap<>(aliasToCanonical);
    }


    /**
     * Retourne la liste des arrêts correspondant à la requête, triés par pertinence.
     *
     * @param query Requête utilisateur
     * @param maxResults Nombre maximum de résultats à retourner
     * @return Liste des noms d'arrêts pertinents (noms principaux)
     */
    public List<String> stopsMatching(String query, int maxResults) {
        if (query == null || query.isBlank()) {
            return canonicalStops.subList(0, Math.min(maxResults, canonicalStops.size()));
        }

        String[] subQueries = query.trim().split("\\s+");
        boolean isExactUpperCaseQuery = query.matches("[A-Z]{2,}");
        Map<String, Integer> matchesWithScores = new HashMap<>();

        Set<String> allNames = new HashSet<>(canonicalStops);
        allNames.addAll(aliasToCanonical.keySet());

        for (String name : allNames) {
            if (isExactUpperCaseQuery && !name.contains(query)) continue;

            boolean allMatched = true;
            int totalScore = 0;

            for (String subQuery : subQueries) {
                Pattern pattern = createPatternForSubQuery(subQuery);
                Matcher matcher = pattern.matcher(name);
                if (matcher.find()) {
                    totalScore += calculateScore(subQuery, name, matcher.start(), matcher.end());
                } else {
                    allMatched = false;
                    break;
                }
            }

            if (allMatched) {
                String canonicalName = aliasToCanonical.getOrDefault(name, name);
                matchesWithScores.put(canonicalName,
                        Math.max(matchesWithScores.getOrDefault(canonicalName, 0)
                                , totalScore));
            }
        }

        return canonicalStops.stream()
                .filter(matchesWithScores::containsKey)
                .sorted(Comparator.comparingInt((String name) ->
                        matchesWithScores.get(name)).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }


    private Pattern createPatternForSubQuery(String subQuery) {
        StringBuilder sb = new StringBuilder();
        for (char c : subQuery.toCharArray()) {
            switch (Character.toLowerCase(c)) {
                case 'a' -> sb.append("[aáàâä]");
                case 'c' -> sb.append("[cç]");
                case 'e' -> sb.append("[eéèêë]");
                case 'i' -> sb.append("[iíìîï]");
                case 'o' -> sb.append("[oóòôö]");
                case 'u' -> sb.append("[uúùûü]");
                default -> sb.append(Pattern.quote(Character.toString(c)));
            }
        }
        return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
    }

    private int calculateScore(String subQuery, String stopName, int start, int end) {
        int baseScore = Math.max(1, (subQuery.length() * 100) / stopName.length());
        boolean isWordStart = start == 0 || !Character.isLetter(stopName.charAt(start - 1));
        boolean isWordEnd = end >= stopName.length() || !Character.isLetter(stopName.charAt(end));

        int multiplier = 1;
        if (isWordStart) multiplier *= 4;
        if (isWordEnd) multiplier *= 2;

        return baseScore * multiplier;
    }

    /**
     * Construit un StopIndex à partir d'un fichier CSV de noms d'arrêts et alias.
     *
     * @param csvFile Fichier CSV avec nom principal suivi de ses alias
     * @return StopIndex initialisé avec les données du fichier
     * @throws IOException si une erreur de lecture survient
     */
    public static StopIndex of(Path csvFile) throws IOException {
        List<String> canonicalStops = new ArrayList<>();
        Map<String, String> aliasToCanonical = new HashMap<>();

        List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] parts = line.split(";");
            String canonical = parts[0].trim();
            if (!canonical.isEmpty()) {
                canonicalStops.add(canonical);
            }
            for (int i = 1; i < parts.length; i++) {
                String alias = parts[i].trim();
                if (!alias.isEmpty()) {
                    aliasToCanonical.put(alias, canonical);
                }
            }
        }
        return new StopIndex(canonicalStops, aliasToCanonical);
    }
}
