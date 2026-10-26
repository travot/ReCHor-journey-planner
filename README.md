# ReCHor: Public Transport Journey Planner

A desktop application that finds the best public transport journeys across Switzerland. Choose a departure stop, an arrival stop, a date and a time, and ReCHor shows every journey worth considering, from the earliest arrival to the fewest changes, on the real Swiss timetable.

> Built by Alban Thèves and Tom Ravot for **EPFL CS-108 (Practice of Object-Oriented Programming)**, spring 2025. Java 21 · JavaFX.

**Original development repository (full commit history of both authors):** [github.com/FireII/ReCHor](https://github.com/FireII/ReCHor)

## What it does

1. **Search stops** with autocomplete. Matching ignores case and accents and also understands station aliases.
2. **Pick a date and a time.** The journey list updates automatically whenever the departure, the arrival, the date or the time changes.
3. **Compare journeys.** Results are Pareto-optimal: a journey is shown only if no other journey is at least as good on departure time, arrival time and number of changes. The list scrolls to the journey closest to the requested time.
4. **Open the detail view** of a journey: every leg with platforms and times, walking and transfer legs, and an expandable list of intermediate stops.
5. **Export** a journey to your calendar as an iCalendar (`.ics`) event, or view it on a map (GeoJSON sent to uMap).

The interface is in French, as the project was specified.

## How it works

The application is built in three layers, each independent of the one above it.

| Layer | Package | Role |
|---|---|---|
| Data | `timetable`, `timetable.mapped` | Timetable model (stations, platforms, routes, trips, transfers, connections) and a memory-mapped binary implementation |
| Logic | `journey` | Routing, Pareto fronts, journey reconstruction, iCalendar and GeoJSON conversion |
| Interface | `gui` | JavaFX views bound to the logic through observable values |

**Data layer.** The timetable is stored as binary files that are memory-mapped instead of parsed. A small abstraction (`Structure` and `StructuredBuffer`) describes a record layout once, with typed fields, and reads values directly from the buffer. Each table (`BufferedStations`, `BufferedConnections`, and so on) is a thin view on top of it. Connections and trips are loaded per date, and a `CachedTimeTable` keeps the most recently requested date in memory.

**Routing.** `Router` implements a profile-based connection scan. For a given date and arrival station, it scans the day's connections once and builds, for every station, a *profile*: the set of Pareto-optimal ways to reach the destination. A journey is described by its criteria (departure time, arrival time, number of changes), and each set of criteria is **packed into a single 64-bit `long`**: 12 bits for departure, 12 bits for arrival, 7 bits for changes and 32 bits of payload used to rebuild the journey. This makes dominance checks a few bit operations, and `ParetoFront` stores its sets as sorted `long[]` arrays. The last computed profile is kept in memory, so changing only the departure stop or the time does not recompute the day.

**Journey reconstruction.** `JourneyExtractor` walks a profile from the departure stop and turns the packed payloads back into a full `Journey`: an immutable list of legs, each either a transport leg (with intermediate stops) or a foot leg. `Journey.Leg` is a sealed interface, so every kind of leg is handled explicitly.

**Interface.** `Main` wires everything with JavaFX bindings: the query fields produce observable values, a binding recomputes the list of journeys when they change, and the summary and detail views react to the selected journey. Exports use two small purpose-built classes: a `Json` sealed interface for GeoJSON, and an `IcalBuilder` for calendar events.

## Quality

- **Immutability and safety:** records everywhere, defensive copies, and a `Preconditions` helper that validates arguments at every public entry point.
- **Documentation:** Javadoc on all public classes and methods.
- **Tests:** 130+ unit tests covering bit manipulation, packed criteria, Pareto fronts, profiles, the buffered tables, journey conversion and formatting. Two integration tests (`RouterTest`, `JourneyExtractorTest`) also need the timetable data, and `JourneyExtractorTest` reads a reference profile, `test/resources/profiles/profile_2025-03-18_11486.txt`, which is available in the original repository.
- **Process:** about 250 commits over roughly three months, built incrementally in eleven milestones.

## Project structure

```
src/
├── module-info.java
└── ch/epfl/rechor/
    ├── timetable/          Timetable model, caching
    │   └── mapped/         Memory-mapped binary implementation
    ├── journey/            Router, ParetoFront, Profile, Journey, converters
    ├── gui/                JavaFX views (query, summary, detail)
    └── *.java              Shared utilities (bit packing, StopIndex, Json, IcalBuilder, FormatterFr)
resources/                  Vehicle icons and stylesheets
test/                       Unit and integration tests
```

## Run

Requires **Java 21** and **JavaFX**.

1. **Get the timetable data.** The pre-built binary timetable (one week, 5 May to 11 May 2025, about 250 MB) is in the `timetable/` folder of the [original repository](https://github.com/FireII/ReCHor). Copy it to the project root, so that you have `timetable/stations.bin`, `timetable/2025-05-26/connections.bin`, and so on. The data is not stored in this repository because of its size.
2. **Open the project in IntelliJ IDEA.** Mark `src` as *Sources Root* and `test` as *Test Sources Root*, and add the JavaFX SDK as a library.
3. **Run** `ch.epfl.rechor.gui.Main`. Choose a date within the week above.

## Development history

This repository is a curated copy for presentation, without the course-provided files and the large timetable data. The project was developed in the [original repository, FireII/ReCHor](https://github.com/FireII/ReCHor), where the full commit history of both authors is public. Code comments and Javadoc are in French (course language).

## Authors

Alban Thèves and Tom Ravot
