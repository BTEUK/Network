package me.bteuk.network.utils.progression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelTest {

    Level level;

    @BeforeEach
    void init() {
        level = new Level("test");
    }

    @Test
    void testGetThreshold() {

        assertEquals(16, level.getThreshold(2));
        assertEquals(38, level.getThreshold(3));
        assertEquals(65, level.getThreshold(5));
        assertEquals(105, level.getThreshold(10));
        assertEquals(166, level.getThreshold(25));

    }

    @Test
    void testReachedNextLevel() {

        assertTrue(level.reachedNextLevel(1, 20));
        assertTrue(level.reachedNextLevel(4, 65));

        assertFalse(level.reachedNextLevel(4, 64));
        assertFalse(level.reachedNextLevel(10, 100));

    }

    @Test
    void testGetLeftoverExp() {

        assertEquals(4, level.getLeftoverExp(2, 20));
        assertEquals(0, level.getLeftoverExp(5, 65));

    }
}