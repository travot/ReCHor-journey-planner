package ch.epfl.rechor.timetable.mapped;

import ch.epfl.rechor.timetable.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;

/**
 * Représente un horaire de transport public dont les données sont stockées dans des fichiers.
 * Cet enregistrement implémente l'interface {@link TimeTable} et permet d'accéder aux différentes
 * informations relatives aux stations, itinéraires et transferts.
 *
 * @author Alban (401009)
 */
public record FileTimeTable(Path directory,
                            List<String> stringTable,
                            Stations stations,
                            StationAliases stationAliases,
                            Platforms platforms,
                            Routes routes,
                            Transfers transfers) implements TimeTable {

    /**
     * Crée une instance de {@code FileTimeTable} à partir du dossier contenant les fichiers
     * des données horaires.
     *
     * @param directory le chemin d'accès au dossier contenant les fichiers des données horaires.
     * @return une nouvelle instance de {@code FileTimeTable}.
     * @throws IOException en cas d'erreur d'entrée/sortie lors de la lecture des fichiers.
     */
    public static TimeTable in(Path directory) throws IOException {

        List<String> stringTable = List.copyOf(Files.readAllLines(
                directory.resolve("strings.txt"), StandardCharsets.ISO_8859_1));

        ByteBuffer stationsBuffer = mapFile(directory, "stations.bin");
        ByteBuffer stationAliasesBuffer = mapFile(directory, "station-aliases.bin");
        ByteBuffer platformsBuffer = mapFile(directory, "platforms.bin");
        ByteBuffer routesBuffer = mapFile(directory, "routes.bin");
        ByteBuffer transfersBuffer = mapFile(directory, "transfers.bin");

        BufferedStations stations = new BufferedStations(stringTable, stationsBuffer);
        BufferedStationAliases stationAliases = new BufferedStationAliases(
                stringTable, stationAliasesBuffer);
        BufferedPlatforms platforms = new BufferedPlatforms(stringTable, platformsBuffer);
        BufferedRoutes routes = new BufferedRoutes(stringTable, routesBuffer);
        BufferedTransfers transfers = new BufferedTransfers(transfersBuffer);

        return new FileTimeTable(directory, stringTable, stations, stationAliases, platforms,
                routes, transfers);

    }

    @Override
    public Stations stations() {
        return stations;
    }

    @Override
    public StationAliases stationAliases() {
        return stationAliases;
    }

    @Override
    public Platforms platforms() {
        return platforms;
    }

    @Override
    public Routes routes() {
        return routes;
    }

    @Override
    public Transfers transfers() {
        return transfers;
    }

    /**
     * Retourne les trajets disponibles pour une date donnée.
     *
     * @param date la date pour laquelle récupérer les trajets.
     * @return les trajets disponibles pour la date spécifiée.
     * @throws UncheckedIOException en cas d'erreur d'entrée/sortie lors de la lecture du fichier.
     */
    @Override
    public Trips tripsFor(LocalDate date) {
        try (FileChannel tripsChannel = FileChannel.open(
                directory.resolve(date.toString()).resolve("trips.bin"))) {
            return new BufferedTrips(stringTable, tripsChannel.map(FileChannel.MapMode.READ_ONLY,
                    0, tripsChannel.size()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retourne les correspondances disponibles pour une date donnée.
     *
     * @param date la date pour laquelle récupérer les correspondances.
     * @return les correspondances disponibles pour la date spécifiée.
     * @throws UncheckedIOException en cas d'erreur d'entrée/sortie lors de la lecture des fichiers.
     */
    @Override
    public Connections connectionsFor(LocalDate date) {
        try (
                FileChannel connectionsChannel = FileChannel.open(
                        directory.resolve(date.toString()).resolve("connections.bin"));
                FileChannel connectionSuccChannel = FileChannel.open(
                        directory.resolve(date.toString()).resolve("connections-succ.bin"))
        ) {
            return new BufferedConnections(
                    connectionsChannel.map(FileChannel.MapMode.READ_ONLY,
                            0, connectionsChannel.size()),
                    connectionSuccChannel.map(FileChannel.MapMode.READ_ONLY,
                            0, connectionSuccChannel.size())
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Mappe le fichier binaire en mémoire et retourne son contenu sous forme de {@link ByteBuffer}.
     *
     * @param filename le nom du fichier à mapper, relatif au répertoire courant
     * @return un {@link ByteBuffer} en lecture seule représentant le contenu du fichier
     * @throws IOException si une erreur survient lors de l'ouverture ou du mapping du fichier
     */
    private static ByteBuffer mapFile(Path directory, String filename) throws IOException {
        try (FileChannel channel = FileChannel.open(
                directory.resolve(filename), StandardOpenOption.READ)) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }
}
