package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.IcalBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Classe utilitaire pour convertir un objet Journey en format iCalendar.
 * @author Tom (392844)
 */
public class JourneyIcalConverter {

    /**
     * Constructeur privé pour empêcher l'instanciation de la classe utilitaire.
     */
    private JourneyIcalConverter() {
    }
    /**
     * Convertit un objet Journey en une chaîne iCalendar.
     * @param journey l'objet Journey à convertir
     * @return une représentation de l'événement en format iCalendar
     */
    public static String toIcalendar(Journey journey) {
        String uid = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        String dtstamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        String dtstart = journey.depTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        String dtend = journey.arrTime().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
        String summary = journey.depStop().name() + " → " + journey.arrStop().name();
        StringJoiner stringJoiner = new StringJoiner("\\n");

        for (int i = 0; i < journey.legs().size(); i++) {
            Journey.Leg leg = journey.legs().get(i);
            switch (leg) {
                case Journey.Leg.Foot footLeg ->
                    stringJoiner.add(FormatterFr.formatLeg(footLeg));
                case Journey.Leg.Transport transportLeg ->
                    stringJoiner.add(FormatterFr.formatLeg(transportLeg));
            }
        }

        IcalBuilder builder = new IcalBuilder();

        builder.begin(IcalBuilder.Component.VCALENDAR)  // Indique le début du fichier iCalendar
                .add(IcalBuilder.Name.VERSION, "2.0") // Version du format iCalendar
                .add(IcalBuilder.Name.PRODID, "ReCHor") // Identifiant du générant l'icalendar
                .begin(IcalBuilder.Component.VEVENT) // Débute la définition d'un événement
                .add(IcalBuilder.Name.UID, uid) // UID unique
                .add(IcalBuilder.Name.DTSTAMP, dtstamp) // Date de création de l'événement
                .add(IcalBuilder.Name.DTSTART, dtstart) // Heure de début de l'événement
                .add(IcalBuilder.Name.DTEND, dtend) // Heure de fin de l'événement
                .add(IcalBuilder.Name.SUMMARY, summary) // Résumé de l'événement
                .add(IcalBuilder.Name.DESCRIPTION, stringJoiner.toString()) // Description du trajet
                .end() // Fin de l'événement
                .end(); // Fin du fichier iCalendar

        return builder.build();
    }
}

