package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyGeoJsonConverter;
import ch.epfl.rechor.journey.JourneyIcalConverter;
import ch.epfl.rechor.journey.Stop;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ch.epfl.rechor.gui.VehicleIcons.iconFor;
/**
 * Interface utilisateur affichant le détail d’un voyage.
 * Permet d’afficher graphiquement les différentes étapes
 * d’un voyage, y compris les transports, les arrêts intermédiaires,
 * et les correspondances. Fournit également des boutons
 * pour exporter le voyage en format iCalendar (.ics) ou en carte (.geojson).
 * @author Alban (401009)
 */
public record DetailUI(Node rootNode) {
    /**
     * Crée une interface de détail liée à un observable de voyage.
     * @param journeyObservableValue l'observable contenant un {@link Journey}
     * @return l'interface {@link DetailUI} correspondant au voyage
     */
    public static DetailUI create(ObservableValue<Journey> journeyObservableValue) {
        ScrollPane rootScrollPane = new ScrollPane();
        rootScrollPane.setId("detail");
        rootScrollPane.getStylesheets().add(new File("resources/detail.css")
                .toURI().toString());

        StackPane rootStackPane = new StackPane();
        rootScrollPane.setContent(rootStackPane);

        journeyObservableValue.addListener((obs, oldJ
                , newJ) ->
                updateContent(rootStackPane, newJ));

        updateContent(rootStackPane, journeyObservableValue.getValue());

        return new DetailUI(rootScrollPane);
    }

    private static void updateContent(StackPane rootStackPane, Journey journey) {
        rootStackPane.getChildren().clear();

        if (journey == null) {
            VBox layoutNoJourneyVBox = new VBox(new Text("Aucun voyage"));
            layoutNoJourneyVBox.setId("no-journey");
            rootStackPane.getChildren().add(layoutNoJourneyVBox);
            return;
        }

        List<Journey.Leg> legs = journey.legs();
        VBox layoutJourneyVBox = new VBox();

        HBox buttonHBox = new HBox();
        buttonHBox.setId("buttons");

        Button calendarButton = getButton(journey);

        Button mapButton = getButton(legs);

        buttonHBox.setSpacing(5);
        buttonHBox.getChildren().addAll(mapButton, calendarButton);

        StackPane journeyStackPane = new StackPane();
        Pane annotationsPane = new Pane();
        annotationsPane.setId("annotations");

        List<Pair<Circle, Circle>> linesDraw = new ArrayList<>();

        GridPane legsGridPane = getGridPane(annotationsPane, linesDraw);
        int currentRow = 0;

        for (Journey.Leg leg : journey.legs()) {
            if (leg instanceof Journey.Leg.Foot footLeg) {
                Text footText = new Text(FormatterFr.formatLeg(footLeg));
                GridPane.setColumnSpan(footText, 2);
                legsGridPane.add(footText, 2, currentRow);
                currentRow++;
            } else if (leg instanceof Journey.Leg.Transport transportLeg) {
                boolean hasIntermediateStops = !transportLeg.intermediateStops().isEmpty();

                Text depTimeText = new Text(FormatterFr.formatTime(transportLeg.depTime()));
                depTimeText.getStyleClass().add("departure");
                Circle departureCircle = new Circle(3);
                Text depStation = new Text(transportLeg.depStop().name());
                Text depPlatform = new Text(FormatterFr.formatPlatformName(transportLeg.depStop()));
                depPlatform.getStyleClass().add("departure");

                legsGridPane.add(depTimeText, 0, currentRow);
                legsGridPane.add(departureCircle, 1, currentRow);
                legsGridPane.add(depStation, 2, currentRow);
                legsGridPane.add(depPlatform, 3, currentRow);

                ImageView vehicleIcon = new ImageView(iconFor(transportLeg.vehicle()));
                vehicleIcon.setFitWidth(31);
                vehicleIcon.setFitHeight(31);

                Text routeDest = new Text(FormatterFr.formatRouteDestination(transportLeg));
                GridPane.setColumnSpan(routeDest, 2);
                legsGridPane.add(vehicleIcon, 0, currentRow + 1);
                legsGridPane.add(routeDest, 2, currentRow + 1);

                int linesUsed = 3;

                if (hasIntermediateStops) {
                    Accordion accordion = new Accordion();
                    VBox content = new VBox();
                    GridPane intermediateGrid = new GridPane();
                    intermediateGrid.getStyleClass().add("intermediate-stops");

                    int stopRow = 0;
                    for (Journey.Leg.IntermediateStop stop : transportLeg.intermediateStops()) {
                        intermediateGrid.add(new Text(FormatterFr.formatTime(stop.arrTime()))
                                , 0, stopRow);
                        intermediateGrid.add(new Text(FormatterFr.formatTime(stop.depTime()))
                                , 1, stopRow);
                        intermediateGrid.add(new Text(stop.stop().name()), 2, stopRow);
                        stopRow++;
                    }

                    content.getChildren().add(intermediateGrid);
                    TitledPane pane = new TitledPane(transportLeg.intermediateStops().size()
                            + " arrêts, " +
                            FormatterFr.formatDuration(transportLeg.duration()), content);
                    accordion.getPanes().add(pane);
                    GridPane.setColumnSpan(accordion, 2);
                    legsGridPane.add(accordion, 2, currentRow + 2);
                    GridPane.setRowSpan(vehicleIcon, 2);
                    linesUsed = 4;
                }

                Text arrTimeText = new Text(FormatterFr.formatTime(transportLeg.arrTime()));
                Circle arrivalCircle = new Circle(3);
                Text arrStation = new Text(transportLeg.arrStop().name());
                Text arrPlatform = new Text(FormatterFr.formatPlatformName(transportLeg.arrStop()));

                legsGridPane.add(arrTimeText, 0, currentRow + linesUsed - 1);
                legsGridPane.add(arrivalCircle, 1, currentRow + linesUsed - 1);
                legsGridPane.add(arrStation, 2, currentRow + linesUsed - 1);
                legsGridPane.add(arrPlatform, 3, currentRow + linesUsed - 1);

                linesDraw.add(new Pair<>(departureCircle, arrivalCircle));

                currentRow += linesUsed;
            }
        }

        journeyStackPane.getChildren().addAll(annotationsPane, legsGridPane);
        layoutJourneyVBox.getChildren().addAll(journeyStackPane, buttonHBox);
        rootStackPane.getChildren().add(layoutJourneyVBox);
    }

    /**
     * Construit une grille affichant les arrêts, avec un calque d'annotations permettant de tracer
     * des lignes entre les cercles représentant départ et arrivée.
     *
     * @param annotationsPane le panneau où seront ajoutées les lignes d'annotation
     * @param linesDraw       la liste des paires de cercles à relier par une ligne
     * @return une nouvelle instance de {@link GridPane} configurée pour afficher les
     * jambes du voyage
     */
    private static GridPane getGridPane(Pane annotationsPane, List<Pair<Circle, Circle>> linesDraw)
    {
        GridPane legsGridPane = new GridPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                annotationsPane.getChildren().clear();
                for (Pair<Circle, Circle> pair : linesDraw) {
                    Circle dep = pair.getKey();
                    Circle arr = pair.getValue();
                    Bounds depBounds = dep.getBoundsInParent();
                    Bounds arrBounds = arr.getBoundsInParent();
                    Line line = new Line(depBounds.getCenterX(), depBounds.getCenterY()
                            , arrBounds.getCenterX(), arrBounds.getCenterY());
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(2);
                    annotationsPane.getChildren().add(line);
                }
            }
        };

        legsGridPane.setId("legs");
        return legsGridPane;
    }

    /**
     * Crée un bouton « Carte » qui ouvre dans le navigateur la carte du trajet.
     *
     * @param legs la liste des étapes du voyage
     * @return un bouton JavaFX configuré avec l'action d'affichage de la carte
     */
    private static Button getButton(List<Journey.Leg> legs) {
        Button mapButton = new Button("Carte");
        mapButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                try {
                    List<Stop> stopsJSON = new ArrayList<>();
                    for (Journey.Leg leg : legs) {
                        stopsJSON.add(leg.depStop());
                        stopsJSON.add(leg.arrStop());
                    }
                    stopsJSON = stopsJSON.stream().distinct().collect(Collectors.toList());

                    URI uri = new URI("https", "umap.osm.ch", "/fr/map",
                            "data=" + JourneyGeoJsonConverter.toGeoJson(stopsJSON)
                            , null);
                    Desktop.getDesktop().browse(uri);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        });
        return mapButton;
    }

    /**
     * Crée un bouton « Calendrier » permettant d’exporter le voyage au format iCalendar (.ics).
     *
     * @param journey le voyage à exporter
     * @return un bouton JavaFX configuré avec l'action d'export iCalendar
     */
    private static Button getButton(Journey journey) {
        Button calendarButton = new Button("Calendrier");
        calendarButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Enregistrement de l'événement iCalendar");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                        "Fichiers iCalendar (*.ics)", "*.ics"));
                fileChooser.setInitialFileName("voyage_" + journey.depTime().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".ics");
                File selectedFile = fileChooser.showSaveDialog(calendarButton.getScene()
                        .getWindow());
                if (selectedFile != null) {
                    String iCalendarString = JourneyIcalConverter.toIcalendar(journey);
                    try {
                        Files.writeString(selectedFile.toPath(), iCalendarString);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        return calendarButton;
    }
}
