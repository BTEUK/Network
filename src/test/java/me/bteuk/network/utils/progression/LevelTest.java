package me.bteuk.network.utils.progression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static me.bteuk.network.utils.progression.Level.getLeftoverExp;
import static me.bteuk.network.utils.progression.Level.getThreshold;
import static me.bteuk.network.utils.progression.Level.reachedNextLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {

    @BeforeEach
    void init() {

    }

    @Test
    void testGetThreshold() {

        assertEquals(16, getThreshold(2));
        assertEquals(38, getThreshold(3));
        assertEquals(65, getThreshold(5));
        assertEquals(105, getThreshold(10));
        assertEquals(166, getThreshold(25));

    }

    @Test
    void testReachedNextLevel() {

        assertTrue(reachedNextLevel(1, 20));
        assertTrue(reachedNextLevel(4, 65));

        assertFalse(reachedNextLevel(4, 64));
        assertFalse(reachedNextLevel(10, 100));

    }

    @Test
    void testGetLeftoverExp() {

        assertEquals(4, getLeftoverExp(2, 20));
        assertEquals(0, getLeftoverExp(5, 65));

    }
}