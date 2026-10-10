package ch.epfl.rechor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StopIndexTest {

    @Test
    void test() {
        String a = "salam";
        int max = 2;

        List<String> stops = List.of("sàlutation", "sal");
        Map<String, String> aliases = new HashMap<>();
        aliases.put("salam", "sàlutation");
        aliases.put("sal", "sal");

        StopIndex stopIndex = new StopIndex(stops, aliases);

        System.out.println(stopIndex.stopsMatching(a, max));
    }
}
