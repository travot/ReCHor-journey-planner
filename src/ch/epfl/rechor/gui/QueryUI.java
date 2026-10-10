package ch.epfl.rechor.gui;
import ch.epfl.rechor.StopIndex;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente l'interface utilisateur permettant de saisir une requête de trajet.
 * Elle inclut les champs de saisie pour l'arrêt de départ, l'arrêt d'arrivée,
 * la date de départ et l'heure de départ.
 * Cette classe est un record qui regroupe les composants graphiques nécessaires
 * pour la recherche de trajets.
 * @param rootNode  le nœud racine de cette interface (VBox).
 * @param depStopO  l'observable contenant le nom de l'arrêt de départ
 * @param arrStopO  l'observable contenant le nom de l'arrêt d'arrivée
 * @param dateO     l'observable contenant la date de départ sélectionnée
 * @param timeO     l'observable contenant l'heure de départ sélectionnée
 * @author Tom (392844)
 */
public record QueryUI(
        Node rootNode,
        ObservableValue<String> depStopO,
        ObservableValue<String> arrStopO,
        ObservableValue<LocalDate> dateO,
        ObservableValue<LocalTime> timeO) {

    /**
     * Crée une nouvelle instance de {@code QueryUI} avec tous les composants nécessaires
     * pour que l'utilisateur saisisse sa requête de trajet.
     * @param stopIndex l'index des arrêts à utiliser pour l'autocomplétion
     * @return une instance de {@code QueryUI} prête à être intégrée à l'interface graphique
     */
    public static QueryUI create(StopIndex stopIndex) {
        VBox mainContainer = new VBox();
        mainContainer.getStylesheets().add("query.css");

        HBox stopFieldsContainer = new HBox();
        HBox dateTimeFieldsContainer = new HBox();

        // Champ pour l'arrêt de départ
        StopField depField = StopField.create(stopIndex);
        TextField depTextField = depField.field();
        depTextField.setPromptText("Nom de l'arrêt de départ");
        Label depLabel = new Label("Départ\u202f:");

        // Champ pour l'arrêt d'arrivée
        StopField arrField = StopField.create(stopIndex);
        TextField arrTextField = arrField.field();
        arrTextField.setPromptText("Nom de l'arrêt d'arrivée");
        Label arrLabel = new Label("Arrivée\u202f:");

        // Bouton d'inversion des arrêts
        Button switchButton = new Button("↔");
        switchButton.setOnAction(event -> {
            String dep = depField.field().getText();
            String arr = arrField.field().getText();
            depField.setTo(arr);
            arrField.setTo(dep);
        });

        stopFieldsContainer.getChildren().addAll(
                depLabel,
                depTextField,
                switchButton,
                arrLabel,
                arrTextField
        );

        // Sélecteur de date
        DatePicker datePicker = new DatePicker();
        datePicker.setId("date");
        datePicker.setValue(LocalDate.now());
        Label datePickerLabel = new Label("Date\u202f:");

        // Champ pour saisir l'heure
        TextField timePicker = new TextField();
        timePicker.setId("time");
        timePicker.setPromptText("HH:mm");

        // Format d'affichage et de parsing de l'heure
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("H:mm");
        LocalTimeStringConverter converter = new LocalTimeStringConverter(displayFormatter
                , parseFormatter);
        TextFormatter<LocalTime> timeFormatter = new TextFormatter<>(converter);

        // Valeur par défaut = heure actuelle, secondes et nanosecondes mises à zéro
        timeFormatter.setValue(LocalTime.now().withSecond(0).withNano(0));
        timePicker.setTextFormatter(timeFormatter);
        Label timePickerLabel = new Label("Heure\u202f:");

        dateTimeFieldsContainer.getChildren().addAll(
                datePickerLabel,
                datePicker,
                timePickerLabel,
                timePicker
        );

        // Ajout des deux parties à la vue principale
        mainContainer.getChildren().addAll(stopFieldsContainer, dateTimeFieldsContainer);

        // Création de l'instance de QueryUI avec tous les composants
        return new QueryUI(
                mainContainer,
                depField.stopO(),
                arrField.stopO(),
                datePicker.valueProperty(),
                timeFormatter.valueProperty()
        );
    }
}
