package ch.epfl.rechor.journey;

import ch.epfl.rechor.timetable.TimeTable;
import ch.epfl.rechor.timetable.mapped.FileTimeTable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class JourneyExtractorTest {
    public static void main(String[] args) throws IOException {
        TimeTable t = FileTimeTable.in(Path.of("timetable"));
        LocalDate date = LocalDate.of(2025, Month.MARCH, 18);
        int arrivalStationId = 11486; //gruyere
        int departureStationId = 7872; //epfl

        Profile p = readProfile(t, date, arrivalStationId);

        List<Journey> journeys = JourneyExtractor.journeys(p, departureStationId);

        System.out.println("Found " + journeys.size() + " journeys.\n");

        if (journeys.isEmpty()) {
            System.out.println("No journeys found for the selected station and date.");
        } else {
            for (int i = 0; i < journeys.size(); i++) {
                System.out.println("Journey #" + i + ":");
                System.out.println(JourneyIcalConverter.toIcalendar(journeys.get(i)));
                System.out.println("--------------------------------------------------\n");
            }
        }
    }

    public static Profile readProfile(TimeTable timeTable,
                                      LocalDate date,
                                      int arrStationId) throws IOException {
        Path path = Path.of("test/resources/profiles/profile_2025-03-18_11486.txt");
        try (BufferedReader r = Files.newBufferedReader(path)) {
            Profile.Builder profileB = new Profile.Builder(timeTable, date, arrStationId);
            int stationId = -1;
            String line;
            while ((line = r.readLine()) != null) {
                stationId += 1;
                if (line.isEmpty()) continue;
                ParetoFront.Builder frontB = new ParetoFront.Builder();
                for (String t : line.split(","))
                    frontB.add(Long.parseLong(t, 16));
                profileB.setForStation(stationId, frontB);
            }
            return profileB.build();
        }
    }
}