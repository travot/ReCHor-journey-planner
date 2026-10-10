package ch.epfl.rechor;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class IcalBuilderTest {

    @Test
    public void testAddMethodWithString() {
        IcalBuilder builder = new IcalBuilder();
        builder.add(IcalBuilder.Name.SUMMARY, "Test Event");
        String result = builder.build();
        System.out.println(result);
        assertTrue(result.contains("SUMMARY:Test Event"));
    }

    @Test
    public void testAddMethodWithDateTime() {
        IcalBuilder builder = new IcalBuilder();
        LocalDateTime dateTime = LocalDateTime.of(2025, 2, 25, 12, 30);
        builder.add(IcalBuilder.Name.DTSTART, dateTime);
        String result = builder.build();
        assertTrue(result.contains("DTSTART:20250225T123000"));
    }

    @Test
    public void testBeginAndEndMethods() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VEVENT);
        builder.end();
        String result = builder.build();
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("END:VEVENT"));
    }

    @Test
    public void testMultipleLinesInString() {
        IcalBuilder builder = new IcalBuilder();
        String longText = "Ceci est un exemple de texte très long qui doit être coupé en plusieurs lignes de 75 caractères maximum pour vérifier le bon fonctionnement de la méthode.";
        builder.add(IcalBuilder.Name.DESCRIPTION, longText);
        String result = builder.build();
        System.out.println(result);
        assertTrue(result.contains("DESCRIPTION:Ceci est un exemple de texte très long qui doit être coupé en p"));
        assertFalse(result.contains("DESCRIPTION:Ceci est un exemple de texte très long qui doit être coupé en pl"));
        assertTrue(result.contains(" lusieurs lignes de 75 caractères maximum pour vérifier le bon fonctionneme"));
    }

    @Test
    public void testEndWithoutBegin() {
        IcalBuilder builder = new IcalBuilder();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, builder::end);
    }

    @Test
    public void testBuildWithUnclosedComponent() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VEVENT);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals("Un composant n'a pas été correctement fermé", thrown.getMessage());
    }

    @Test
    public void testBuildWithValidComponent() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VEVENT);
        builder.add(IcalBuilder.Name.SUMMARY, "Test Event");
        builder.end();
        String result = builder.build();
        assertTrue(result.contains("BEGIN:VEVENT"));
        assertTrue(result.contains("SUMMARY:Test Event"));
        assertTrue(result.contains("END:VEVENT"));
    }
    @Test
    public void testEndOnEmptyComponentList() {
        IcalBuilder builder = new IcalBuilder();
        builder.begin(IcalBuilder.Component.VEVENT);
        builder.end();
        assertThrows(IllegalArgumentException.class, () -> {
            builder.end();
        });
    }
}


