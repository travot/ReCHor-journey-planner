package ch.epfl.rechor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classe permettant de construire un fichier iCalendar (format .ics).
 *
 * @author Tom (392844)
 */
public final class IcalBuilder {

    private final List<Component> components;
    private final StringBuilder builder;

    private static final String CRLF = "\r\n";
    private static final int MAX_LINE_LENGTH = 75;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Constructeur de la classe IcalBuilder.
     */
    public IcalBuilder() {
        this.components = new ArrayList<>();
        this.builder = new StringBuilder();
    }

    /**
     * Enumération représentant les composants de l'iCalendar.
     */
    public enum Component {
        VCALENDAR, VEVENT
    }

    /**
     * Enumération représentant les noms des propriétés utilisées dans iCalendar.
     */
    public enum Name {
        BEGIN, END, PRODID, VERSION, UID, DTSTAMP, DTSTART, DTEND, SUMMARY, DESCRIPTION
    }

    /**
     * Ajoute une propriété avec une valeur donnée au fichier iCalendar.
     *
     * @param name  le nom de la propriété
     * @param value la valeur de la propriété
     * @return l'instance actuelle de IcalBuilder
     */
    public IcalBuilder add(Name name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);

        String prefix = name + ":";
        builder.append(prefix);
        int currentLineLength = prefix.length();

        int index = 0;
        while (index < value.length()) {
            int spaceLeft = MAX_LINE_LENGTH - currentLineLength;
            int end = Math.min(index + spaceLeft, value.length());

            builder.append(value, index, end);
            index = end;

            if (index < value.length()) {
                builder.append(CRLF)
                        .append(" ");
                currentLineLength = 1; // space indent
            }
        }
        builder.append(CRLF);
        return this;
    }

    /**
     * Ajoute une propriété contenant une date et une heure.
     *
     * @param name     le nom de la propriété
     * @param dateTime la valeur date et heure à ajouter
     * @return l'instance actuelle de IcalBuilder
     * @throws NullPointerException si name ou dateTime est null lance exeption
     */
    public IcalBuilder add(Name name, LocalDateTime dateTime) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(dateTime);
        return add(name, dateTime.format(DATE_TIME_FORMATTER));
    }

    /**
     * Commence un nouveau composant iCalendar.
     *
     * @param component le composant à ajouter
     * @return l'instance actuelle de IcalBuilder
     * @throws NullPointerException si component est null lance exeption
     */
    public IcalBuilder begin(Component component) {
        Objects.requireNonNull(component);
        components.add(component);
        builder.append("BEGIN:")
                .append(component)
                .append(CRLF);
        return this;
    }

    /**
     * Termine le dernier composant iCalendar ouvert.
     *
     * @return l'instance actuelle de IcalBuilder
     * @throws IllegalArgumentException si aucun composant n'est ouvert lance erreur
     */
    public IcalBuilder end() {
        if (components.isEmpty()) {
            throw new IllegalArgumentException("Un composant n'a pas été correctement fermé");
        }
        Component component = components.removeLast();
        builder.append("END:")
                .append(component)
                .append(CRLF);
        return this;

    }

    /**
     * Construit et retourne la chaîne de caractères représentant le fichier iCalendar.
     *
     * @return la représentation sous forme de chaîne de caractères de l'iCalendar
     * @throws IllegalArgumentException si des composants n'ont pas été fermés correctement
     */
    public String build() {
        if (!components.isEmpty()) {
            throw new IllegalArgumentException("Un composant n'a pas été correctement fermé");
        }
        return builder.toString();
    }
}