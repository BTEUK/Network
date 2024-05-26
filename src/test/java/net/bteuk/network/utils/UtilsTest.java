package net.bteuk.network.utils;

import net.bteuk.network.lib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void testErrorVarAtStart() {
        Component component = ChatUtils.error("%s message with var at start.", "Error");
        HashMap<String, NamedTextColor> segments = new LinkedHashMap<>();
        segments.put("Error", DARK_RED);
        segments.put(" message with var at start.", RED);
        Component expected = createComponent(segments);

        assertEquals(expected, component);
    }

    @Test
    void testErrorVarAtEnd() {
        Component component = ChatUtils.error("This message ends with a %s", "Var");
        HashMap<String, NamedTextColor> segments = new LinkedHashMap<>();
        segments.put("This message ends with a ", RED);
        segments.put("Var", DARK_RED);
        Component expected = createComponent(segments);

        assertEquals(expected, component);
    }

    @Test
    void testErrorVarAtStartAndEnd() {
        Component component = ChatUtils.error("%s message starts and ends with a %s", "Error", "Var");
        HashMap<String, NamedTextColor> segments = new LinkedHashMap<>();
        segments.put("Error", DARK_RED);
        segments.put(" message starts and ends with a ", RED);
        segments.put("Var", DARK_RED);
        Component expected = createComponent(segments);

        assertEquals(expected, component);
    }

    @Test
    void testErrorMultipleVars() {
        Component component = ChatUtils.error("%s%s %s", "This ", "Error", "Is only vars");
        HashMap<String, NamedTextColor> segments = new LinkedHashMap<>();
        segments.put("This ", DARK_RED);
        segments.put("Error", DARK_RED);
        segments.put(" ", RED);
        segments.put("Is only vars", DARK_RED);
        Component expected = createComponent(segments);

        assertEquals(expected, component);
    }

    @Test
    void testErrorWithWrongNumberOfVars() {
        Component component = ChatUtils.error("This %s message should include %s vars.", "Error");
        HashMap<String, NamedTextColor> segments = new LinkedHashMap<>();
        segments.put("This ", RED);
        segments.put("Error", DARK_RED);
        segments.put(" message should include ", RED);
        segments.put(" vars.", RED);
        Component expected = createComponent(segments);

        assertEquals(expected, component);
    }

    private Component createComponent(HashMap<String, NamedTextColor> segments) {
        Component component = Component.empty();
        for (Map.Entry<String, NamedTextColor> entry : segments.entrySet()) {
            component = component.append(Component.text(entry.getKey(), entry.getValue())
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, false));
        }
        return component;
    }
}