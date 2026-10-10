package ch.epfl.rechor.gui;

import ch.epfl.rechor.FormatterFr;
import ch.epfl.rechor.journey.Journey;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Interface utilisateur affichant la liste résumée des trajets disponibles,
 * sous forme de cellules graphiques interactives.
 * @param rootNode        le nœud racine de l'interface.
 * @param selectedJourneyO la propriété observable contenant le trajet sélectionné
 * @author Tom (392844)
 */
public record SummaryUI(Node rootNode, ObservableValue<Journey> selectedJourneyO) {

    private static final int PADDING = 6;
    private static final int CIRCLE_RADIUS = 3;
    private static final int MARGIN = 10;

    /**
     * Crée l'interface graphique permettant d'afficher une liste de trajets.
     * @param journeysO une propriété observable contenant la liste des trajets disponibles
     * @param refTimeO une propriété observable contenant l'heure de référence
     * @return une instance de {@code SummaryUI}
     */
    public static SummaryUI create(ObservableValue<List<Journey>> journeysO
            , ObservableValue<LocalTime> refTimeO) {
        ObservableList<Journey> observableJourneys = FXCollections.observableArrayList();
        ListView<Journey> listView = new ListView<>(observableJourneys);
        listView.setCellFactory(v -> new JourneyCell());
        listView.getStylesheets().add("summary.css");
        listView.setId("detail");

        ObjectProperty<Journey> selectedJourney = new SimpleObjectProperty<>();
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n)
                        -> selectedJourney.set(n));

        if (journeysO.getValue() != null) {
            observableJourneys.addAll(journeysO.getValue());
        }
        selectClosestJourney(listView, observableJourneys, refTimeO.getValue());

        journeysO.addListener((obs, oldJ
                , newJ) -> {
            observableJourneys.clear();
            if (newJ != null) {
                observableJourneys.addAll(newJ);
                selectClosestJourney(listView, newJ, refTimeO.getValue());
            }
        });

        refTimeO.addListener((obs, oldT
                , newT) -> {
            List<Journey> journeys = journeysO.getValue();
            if (journeys != null) {
                selectClosestJourney(listView, journeys, newT);
            }
        });

        BorderPane root = new BorderPane(listView);
        return new SummaryUI(root, selectedJourney);
    }

    /**
     * Sélectionne dans la liste le trajet dont l'heure de départ
     * est la plus proche de l'heure de référence.
     * @param listView la vue contenant la liste des trajets
     * @param journeys la liste des trajets disponibles
     * @param refTime l'heure de référence
     */
    private static void selectClosestJourney(ListView<Journey> listView, List<Journey> journeys
            , LocalTime refTime) {
        if (journeys == null || journeys.isEmpty() || refTime == null) return;
        for (int i = 0; i < journeys.size(); i++) {
            if (!journeys.get(i).depTime().toLocalTime().isBefore(refTime)) {
                listView.getSelectionModel().select(i);
                int index = i;
                javafx.application.Platform.runLater(() -> listView.scrollTo(index));
                return;
            }
        }
        listView.getSelectionModel().select(journeys.getLast());
        listView.scrollTo(journeys.size() - 1);
    }

    /**
     * Cellule graphique représentant un trajet dans la liste.
     */
    private static class JourneyCell extends ListCell<Journey> {
        private final BorderPane container = new BorderPane();
        private final ImageView vehicleIcon = new ImageView();
        private final Text routeText = new Text();
        private final Text depText = new Text();
        private final Text arrText = new Text();
        private final Text durationText = new Text();
        private final Pane timelinePane = createTimelinePane();

        private Journey currentJourney = null;

        /**
         * Construit une cellule de trajet vide.
         */
        public JourneyCell() {
            vehicleIcon.setFitWidth(20);
            vehicleIcon.setFitHeight(20);
            depText.getStyleClass().add("departure");

            HBox top = new HBox(vehicleIcon, routeText);
            top.getStyleClass().add("route");

            HBox bottom = new HBox(durationText);
            bottom.getStyleClass().add("duration");

            container.setTop(top);
            container.setLeft(depText);
            container.setRight(arrText);
            container.setCenter(timelinePane);
            container.setBottom(bottom);
            container.setPadding(new Insets(PADDING));
            container.getStyleClass().add("journey");
        }

        @Override
        protected void updateItem(Journey journey, boolean empty) {
            super.updateItem(journey, empty);
            if (empty || journey == null) {
                setGraphic(null);
                currentJourney = null;
                return;
            }

            if (journey.equals(currentJourney)) {
                return;
            }
            currentJourney = journey;

            timelinePane.getChildren().clear();
            Line line = new Line();
            timelinePane.getChildren().add(line);

            Circle depCircle = createDot("dep-arr", 0.0);
            Circle arrCircle = createDot("dep-arr", 1.0);
            timelinePane.getChildren().addAll(depCircle, arrCircle);

            Journey.Leg.Transport firstTransport = null;
            for (Journey.Leg leg : journey.legs()) {
                Objects.requireNonNull(leg);
                if (leg instanceof Journey.Leg.Transport t && firstTransport == null) {
                    firstTransport = t;
                } else if (leg instanceof Journey.Leg.Foot walk) {
                    double relX = calcRelX(journey.depTime(), walk.depTime(), journey.duration());
                    if (relX < 0.99) {
                        Circle transfer = createDot("transfer", relX);
                        timelinePane.getChildren().add(transfer);
                    }
                }
            }

            updateRoute(firstTransport);
            depText.setText(FormatterFr.formatTime(journey.depTime()));
            arrText.setText(FormatterFr.formatTime(journey.arrTime()));
            durationText.setText(FormatterFr.formatDuration(journey.duration()));

            setGraphic(container);
        }

        /**
         * Met à jour les informations de la ligne pour le premier transport.
         * @param transport la première section de transport du trajet
         */
        private void updateRoute(Journey.Leg.Transport transport) {
            if (transport != null) {
                routeText.setText(FormatterFr.formatRouteDestination(transport));
                vehicleIcon.setImage(VehicleIcons.iconFor(transport.vehicle()));
            }
        }

        /**
         * Crée un panneau représentant la ligne chronologique du trajet.
         * @return un {@code Pane} personnalisable
         */
        private static Pane createTimelinePane() {
            return new Pane() {
                @Override
                protected void layoutChildren() {
                    double y = getHeight() / 2.0;
                    double xStart = MARGIN;
                    double xEnd = getWidth() - MARGIN;

                    for (Node n : getChildren()) {
                        if (n instanceof Circle c) {
                            Double rel = (Double) c.getUserData();
                            if (rel == null) continue;
                            double x = xStart + rel * (xEnd - xStart);
                            c.setCenterX(x);
                            c.setCenterY(y);
                        } else if (n instanceof Line l) {
                            l.setStartX(xStart);
                            l.setEndX(xEnd);
                            l.setStartY(y);
                            l.setEndY(y);
                        }
                    }
                }
            };
        }

        /**
         * Crée un point circulaire pour l'affichage temporel.
         * @param style le style CSS à appliquer
         * @param relX la position relative sur la ligne (entre 0 et 1)
         * @return un {@code Circle} représentant un point d'arrêt
         */
        private static Circle createDot(String style, double relX) {
            Circle c = new Circle(CIRCLE_RADIUS);
            c.getStyleClass().add(style);
            c.setUserData(relX);
            return c;
        }

        /**
         * Calcule la position relative d'un instant sur la durée du trajet.
         *
         * @param dep heure de départ du trajet
         * @param t instant à évaluer
         * @param dur durée totale du trajet
         * @return un double représentant la position relative (0 = début, 1 = fin)
         */
        private static double calcRelX(LocalDateTime dep, LocalDateTime t, Duration dur) {
            return (double) Duration.between(dep, t).toNanos() / dur.toNanos();
        }
    }
}
