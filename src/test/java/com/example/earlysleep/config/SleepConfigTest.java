package com.example.earlysleep.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SleepConfigTest {

    @Test
    void defaultValuesAreCorrectlyInitialized() {
        SleepConfig config = new SleepConfig();

        assertEquals(19.5, config.sleepStart);
        assertEquals(5.5, config.wakeUpTime);
        assertEquals("50%", config.sleepThreshold);
        assertEquals(-1L, config.sleepDelay);
        assertTrue(config.loadMessageEnabled);
        assertTrue(config.sleepEffectsEnabled);
    }

    @Test
    void manualValueAssignmentsPersistInMemory() {
        SleepConfig config = new SleepConfig();

        config.sleepStart = 20.0;
        config.sleepThreshold = "2";
        config.sleepEffectsEnabled = false;

        assertEquals(20.0, config.sleepStart);
        assertEquals("2", config.sleepThreshold);
        assertTrue(!config.sleepEffectsEnabled);
    }
}