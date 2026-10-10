package ch.epfl.rechor.gui;
import ch.epfl.rechor.StopIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import java.util.List;

/**
 * Champ de saisie pour sélectionner un arrêt via une recherche dynamique.
 * Ce composant permet de taper le nom d'un arrêt et propose des suggestions
 * en temps réel. L'utilisateur peut naviguer dans la liste à l'aide du clavier ou de la souris.
 *  @author Tom (392844)
 */
public record StopField(TextField field, ObservableValue<String> stopO) {
    private static final int MAX_SUGGESTED_STOP_RESULTS = 25;
    private static final double MAX_SUGGESTION_LIST_VIEW_HEIGHT = 240;

    /**
     * Crée un nouveau champ de saisie intelligent pour les arrêts.
     * @param stopIndex l'index contenant les arrêts connus
     * @return une instance de StopField configurée
     */
    public static StopField create(StopIndex stopIndex) {
        TextField textField = new TextField();
        ObjectProperty<String> selectedStop = new SimpleObjectProperty<>();

        Popup popup = createPopup();
        ObservableList<String> suggestions = FXCollections.observableArrayList();
        ListView<String> listView = createListView(suggestions);
        popup.getContent().add(listView);

        // Gestion du clic souris sur une suggestion
        listView.setOnMouseClicked(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                textField.setText(selected);
                textField.positionCaret(selected.length());
                selectedStop.set(selected);
                popup.hide();
                textField.getParent().requestFocus();
            }
        });

        updateSuggestions(stopIndex, textField.getText(), suggestions);

        ChangeListener<String> textListener = createTextChangeListener(stopIndex, listView
                , suggestions);
        ChangeListener<Bounds> boundsListener = createBoundsChangeListener(textField, popup);

        setupFocusBehavior(textField, popup, selectedStop, listView, stopIndex, suggestions
                , textListener, boundsListener);
        setupKeyboardShortcuts(textField, listView, suggestions);

        return new StopField(textField, selectedStop);
    }

    /**
     * Définit le texte du champ et met à jour l'arrêt sélectionné.
     * @param stopName le nom d'arrêt à sélectionner
     */
    public void setTo(String stopName) {
        field.setText(stopName);
        ((ObjectProperty<String>) stopO).set(stopName);
    }

    /**
     * Crée un popup pour afficher la liste des suggestions.
     * @return un objet Popup configuré
     */
    private static Popup createPopup() {
        Popup popup = new Popup();
        popup.setHideOnEscape(false);
        return popup;
    }

    /**
     * Crée une ListView pour afficher les suggestions.
     * @param items les éléments à afficher
     * @return une ListView configurée
     */
    private static ListView<String> createListView(ObservableList<String> items) {
        ListView<String> listView = new ListView<>(items);
        listView.setFocusTraversable(false);
        listView.setMaxHeight(MAX_SUGGESTION_LIST_VIEW_HEIGHT);
        return listView;
    }

    /**
     * Met à jour les suggestions à partir du texte actuel.
     * @param stopIndex   l'index des arrêts
     * @param text        le texte saisi par l'utilisateur
     * @param suggestions la liste observable à remplir
     */
    private static void updateSuggestions(StopIndex stopIndex, String text
            , ObservableList<String> suggestions) {
        List<String> matches = stopIndex.stopsMatching(text, MAX_SUGGESTED_STOP_RESULTS);
        suggestions.setAll(matches);
    }

    /**
     * Crée un listener qui met à jour les suggestions lorsqu'on modifie le texte.
     * @param stopIndex   l'index des arrêts
     * @param listView    la liste de suggestions
     * @param suggestions la liste observable de suggestions
     * @return un ChangeListener à attacher à la propriété text
     */
    private static ChangeListener<String> createTextChangeListener(StopIndex stopIndex
            , ListView<String> listView, ObservableList<String> suggestions) {
        return (obs, oldText, newText) -> {
            updateSuggestions(stopIndex, newText, suggestions);
            listView.getSelectionModel().selectFirst();
        };
    }

    /**
     * Crée un listener qui repositionne le popup lorsqu'on redimensionne le champ.
     * @param textField le champ de texte
     * @param popup     le popup contenant les suggestions
     * @return un ChangeListener à attacher à boundsInLocalProperty
     */
    private static ChangeListener<Bounds> createBoundsChangeListener(TextField textField
            , Popup popup) {
        return (obs, oldBounds, newBounds) -> {
            Bounds screenBounds = textField.localToScreen(newBounds);
            popup.setAnchorX(screenBounds.getMinX());
            popup.setAnchorY(screenBounds.getMaxY());
        };
    }

    /**
     * Configure le comportement du champ lorsqu'il prend ou perd le focus.
     * @param textField       le champ de texte
     * @param popup           le popup contenant les suggestions
     * @param selectedStop    la propriété contenant l'arrêt sélectionné
     * @param listView        la liste des suggestions
     * @param stopIndex       l'index des arrêts
     * @param suggestions     la liste observable de suggestions
     * @param textListener    le listener du texte
     * @param boundsListener  le listener des dimensions
     */
    private static void setupFocusBehavior(
            TextField textField,
            Popup popup,
            ObjectProperty<String> selectedStop,
            ListView<String> listView,
            StopIndex stopIndex,
            ObservableList<String> suggestions,
            ChangeListener<String> textListener,
            ChangeListener<Bounds> boundsListener) {

        textField.focusedProperty().addListener((obs
                , oldFocus, newFocus) -> {
            if (newFocus) {
                handleFocusGain(textField, popup, selectedStop, listView, stopIndex, suggestions
                        , textListener, boundsListener);
            } else {
                handleFocusLoss(textField, popup, selectedStop, listView, textListener
                        , boundsListener);
            }
        });
    }

    /**
     * Gère le comportement lorsqu'on entre dans le champ.
     */
    private static void handleFocusGain(
            TextField field,
            Popup popup,
            ObjectProperty<String> selected,
            ListView<String> listView,
            StopIndex stopIndex,
            ObservableList<String> suggestions,
            ChangeListener<String> textListener,
            ChangeListener<Bounds> boundsListener) {

        String currentValue = selected.get();
        if (currentValue != null) {
            updateSuggestions(stopIndex, currentValue, suggestions);
            field.setText(currentValue);
            listView.getSelectionModel().select(currentValue);
        }

        popup.show(field.getParent(), 0, 0);
        field.textProperty().addListener(textListener);
        field.boundsInLocalProperty().addListener(boundsListener);
    }

    /**
     * Gère le comportement lorsqu'on sort du champ.
     */
    private static void handleFocusLoss(
            TextField field,
            Popup popup,
            ObjectProperty<String> selected,
            ListView<String> listView,
            ChangeListener<String> textListener,
            ChangeListener<Bounds> boundsListener) {

        String chosen = listView.getSelectionModel().getSelectedItem();
        selected.set(chosen);
        field.setText(chosen);

        popup.hide();
        field.textProperty().removeListener(textListener);
        field.boundsInLocalProperty().removeListener(boundsListener);
    }

    /**
     * Configure les raccourcis clavier pour naviguer dans la liste et valider une suggestion.
     * @param textField   le champ de texte
     * @param listView    la liste des suggestions
     * @param suggestions la liste observable des suggestions
     */
    private static void setupKeyboardShortcuts(TextField textField, ListView<String> listView
            , ObservableList<String> suggestions) {
        textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case DOWN -> {
                    int current = listView.getSelectionModel().getSelectedIndex();
                    if (current < suggestions.size() - 1) {
                        listView.getSelectionModel().select(current + 1);
                        listView.scrollTo(current + 1);
                    }
                    event.consume();
                }

                case UP -> {
                    int current = listView.getSelectionModel().getSelectedIndex();
                    if (current > 0) {
                        listView.getSelectionModel().select(current - 1);
                        listView.scrollTo(current - 1);
                    }
                    event.consume();
                }

                case ENTER, TAB -> {
                    String selected = listView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        textField.setText(selected);
                        textField.positionCaret(selected.length());
                    }
                    textField.getParent().requestFocus();
                    event.consume();
                }

                case ESCAPE -> {
                    listView.getSelectionModel().clearSelection();
                    listView.getScene().getWindow().hide();
                    event.consume();
                }
            }
        });
    }
}