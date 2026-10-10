package ch.epfl.rechor.gui;

import ch.epfl.rechor.StopIndex;
import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.JourneyExtractor;
import ch.epfl.rechor.journey.Profile;
import ch.epfl.rechor.journey.Router;
import ch.epfl.rechor.timetable.CachedTimeTable;
import ch.epfl.rechor.timetable.Stations;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * ReCHor – Application principale de planification d’itinéraires ferroviaires.
 * Initialise l’interface utilisateur, connecte les composants et orchestre la logique métier.
 * Ce point d’entrée JavaFX configure la scène principale, établit les liaisons avec le modèle de données
 * et active le moteur de recherche de trajets.
 * @author Alban (401009)
 */
public class Main extends Application {
    private static final String WINDOW_TITLE = "ReCHor";
    private static final String TIMETABLE_DIR = "timetable";

    private ObservableValue<List<Journey>> journeysObservable;

    /**
     * Point d’entrée principal de l’application JavaFX.
     * @param args arguments passés via la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Recherche l’identifiant unique d’une station à partir de son nom.
     * @param stations ensemble des stations disponibles
     * @param stationName nom de la station recherchée
     * @return identifiant numérique associé à la station
     * @throws NoSuchElementException si le nom ne correspond à aucune station
     */
     private static int resolveStationId(Stations stations, String stationName) {
         return java.util.stream.IntStream.range(0, stations.size())
                .filter(i -> stationName.equals(stations.name(i)))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
     }
    /**
     * Initialise l’interface graphique et configure les composants.
     * @param stage fenêtre principale de l’application
     * @throws Exception en cas d’erreur d’initialisation
     */
    @Override
    public void start(Stage stage) throws Exception {
        CachedTimeTable cachedTimeTable = new CachedTimeTable(FileTimeTable.in(
                Path.of(TIMETABLE_DIR)));
        StopIndex stopIndex = initializeStopIndex(cachedTimeTable);

        QueryUI queryUI = QueryUI.create(stopIndex);
        Stations stations = cachedTimeTable.stations();
        Router router = new Router(cachedTimeTable);
        ProfileCache profileCache = new ProfileCache();

        bindJourneyObservable(queryUI, stations, router, profileCache);

        SummaryUI summaryUI = SummaryUI.create(journeysObservable, queryUI.timeO());
        DetailUI detailUI = DetailUI.create(summaryUI.selectedJourneyO());

        SplitPane contentPane = new SplitPane(summaryUI.rootNode(), detailUI.rootNode());

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(queryUI.rootNode());
        mainLayout.setCenter(contentPane);

        Scene scene = new Scene(mainLayout);

        configurePrimaryStage(stage, scene);
        stage.show();
        Platform.runLater(() -> {
            Node depStop = scene.lookup("#depStop");
            if (depStop != null) depStop.requestFocus();
        });
    }

    /**
     * Crée un index des arrêts incluant noms officiels et alias.
     * @param timetable table horaire contenant les stations
     * @return un StopIndex mappant noms et alias
     */
    private StopIndex initializeStopIndex(CachedTimeTable timetable) {
        List<String> officialNames = new ArrayList<>();
        Map<String, String> aliasesMap = new HashMap<>();

        Stations stations = timetable.stations();
        for (int i = 0; i < stations.size(); i++) {
            officialNames.add(stations.name(i));
        }

        var aliases = timetable.stationAliases();
        for (int i = 0; i < aliases.size(); i++) {
            aliasesMap.put(aliases.alias(i), aliases.stationName(i));
        }

        return new StopIndex(officialNames, aliasesMap);
    }

    /**
     * Crée une liaison entre les champs de requête et les trajets calculés.
     * @param queryUI composant de saisie de requête utilisateur
     * @param stations base des stations
     * @param router moteur de calcul d’itinéraires
     * @param cache cache pour les profils de trajet
     */
    private void bindJourneyObservable(QueryUI queryUI, Stations stations, Router router,
                                       ProfileCache cache) {
        journeysObservable = Bindings.createObjectBinding(() -> {
            String dep = queryUI.depStopO().getValue();
            String arr = queryUI.arrStopO().getValue();
            LocalDate date = queryUI.dateO().getValue();

            if (dep == null || dep.isBlank() || arr == null || arr.isBlank() || date == null) {
                return Collections.emptyList();
            }

            int depId = resolveStationId(stations, dep);
            int arrId = resolveStationId(stations, arr);

            Profile profile = cache.fetchProfile(router, date, arrId);
            return JourneyExtractor.journeys(profile, depId);
        }, queryUI.depStopO(), queryUI.arrStopO(), queryUI.dateO());
    }

    /**
     * Configure la fenêtre principale de l’application.
     * @param stage la fenêtre JavaFX
     * @param scene la scène graphique principale
     */
    private void configurePrimaryStage(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
    }

    /**
     * Cache simple pour stocker les profils de trajet en fonction de la date et de la destination.
     * Évite les appels répétitifs au moteur de routage.
     */
    private static class ProfileCache {
        private LocalDate cachedDate;
        private int cachedArrivalId = -1;
        private Profile cachedProfile;

        /**
         * Récupère le profil de trajet en fonction de la date et de la destination.
         * Si les paramètres ont changé, un nouveau profil est généré.
         * @param router moteur de routage
         * @param date date du voyage
         * @param arrivalId identifiant de la gare d’arrivée
         * @return un profil de trajet
         */
        public Profile fetchProfile(Router router, LocalDate date, int arrivalId) {
            if (!Objects.equals(date, cachedDate) || arrivalId != cachedArrivalId) {
                cachedProfile = router.profile(date, arrivalId);
                cachedDate = date;
                cachedArrivalId = arrivalId;
            }
            return cachedProfile;
        }
    }
}
