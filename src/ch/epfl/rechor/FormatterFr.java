package ch.epfl.rechor;

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Classe utilitaire pour formater les informations relatives aux voyages,
 * telles que la durée, l'heure, le nom des plateformes et les trajets.
 *
 * @author Tom (392844)
 */
public final class FormatterFr {

    private static final DateTimeFormatter formatter =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.HOUR_OF_DAY)
                    .appendLiteral('h')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .toFormatter();

    /**
     * Constructeur privé pour empêcher l'instanciation de la classe.
     */
    private FormatterFr() {
    }

    /**
     * Formate la durée en heures et minutes sous la forme "X h Y min".
     *
     * @param duration la durée à formater
     * @return une chaîne de caractères représentant la durée formatée
     */
    public static String formatDuration(Duration duration) {
        int hours = (int) duration.toHours();
        int minutes = (int) duration.toMinutes() % 60;
        return (hours > 0 ? hours + " h " : "") + minutes + " min";
    }

    /**
     * Formate l'heure donnée sous la forme "HHh MMmin" ou "MMmin".
     *
     * @param dateTime l'heure à formater
     * @return une chaîne de caractères représentant l'heure formatée
     */
    public static String formatTime(LocalDateTime dateTime) {
        return formatter.format(dateTime);
    }

    /**
     * Formate le nom de la plateforme d'un arrêt.
     *
     * @param stop l'arrêt dont on veut formater le nom de plateforme
     * @return une chaîne de caractères représentant le nom de la plateforme
     */
    public static String formatPlatformName(Stop stop) {
        String platform = stop.platformName();
        if (platform == null || platform.isEmpty()) {
            return "";
        }
        return Character.isDigit(platform.charAt(0)) ? "voie " + platform : "quai " + platform;
    }

    /**
     * Formate un trajet à pied.
     *
     * @param footLeg le trajet à pied à formater
     * @return une chaîne de caractères représentant le trajet à pied formaté
     */
    public static String formatLeg(Journey.Leg.Foot footLeg) {
        StringBuilder sb = new StringBuilder();
        if (footLeg.isTransfer()) sb.append("changement ");
        else sb.append("trajet à pied ");
        sb.append("(").append(formatDuration(footLeg.duration())).append(")");
        return sb.toString();
    }

    /**
     * Formate un trajet en transport public.
     *
     * @param leg le trajet en transport à formater
     * @return une chaîne de caractères représentant le trajet en transport formaté
     */
    public static String formatLeg(Journey.Leg.Transport leg) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatTime(leg.depTime())).append(" ")
                .append(leg.depStop().name()).append(" ");

        String platformDep = formatPlatformName(leg.depStop());
        if (!platformDep.isEmpty()) {
            sb.append("(").append(platformDep).append(") ");
        }

        sb.append("→ ").append(leg.arrStop().name()).append(" (arr. ")
                .append(formatTime(leg.arrTime()));

        String platformArr = formatPlatformName(leg.arrStop());
        if (!platformArr.isEmpty()) {
            sb.append(" ").append(platformArr);
        }

        sb.append(")");
        return sb.toString();
    }

    /**
     * Formate l'information sur la route et la destination d'un trajet en transport.
     *
     * @param transportLeg le trajet en transport à formater
     * @return une chaîne de caractères représentant la route et la destination formaté
     */
    public static String formatRouteDestination(Journey.Leg.Transport transportLeg) {
        return transportLeg.route() + " Direction " + transportLeg.destination();
    }
}