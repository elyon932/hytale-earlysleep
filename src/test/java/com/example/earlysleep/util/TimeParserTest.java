package com.example.earlysleep.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeParserTest {

    @Test
    void parseTimeToDouble_validFullHour_returnsCorrectDouble() {
        assertEquals(14.0, TimeParser.parseTimeToDouble("14"));
        assertEquals(0.0, TimeParser.parseTimeToDouble("0"));
        assertEquals(23.0, TimeParser.parseTimeToDouble("23"));
        assertEquals(9.0, TimeParser.parseTimeToDouble("09"));
    }

    @Test
    void parseTimeToDouble_validHourAndMinute_returnsCorrectDouble() {
        assertEquals(14.5, TimeParser.parseTimeToDouble("14:30"));
        assertEquals(18.25, TimeParser.parseTimeToDouble("18:15"));
        assertEquals(0.75, TimeParser.parseTimeToDouble("00:45"));
    }

    @Test
    void parseTimeToDouble_invalidFormat_returnsMinusOne() {
        assertEquals(-1.0, TimeParser.parseTimeToDouble("abc"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble("14:"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble(":30"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble("14-30"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble(""));
    }

    @Test
    void parseTimeToDouble_outOfBoundsTime_returnsMinusOne() {
        assertEquals(-1.0, TimeParser.parseTimeToDouble("24"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble("25:00"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble("14:60"));
        assertEquals(-1.0, TimeParser.parseTimeToDouble("-1"));
    }

    @Test
    void formatTime_fullHour_returnsFormattedString() {
        assertEquals("14:00", TimeParser.formatTime(14.0));
        assertEquals("05:00", TimeParser.formatTime(5.0));
    }

    @Test
    void formatTime_fractionalHour_returnsFormattedString() {
        assertEquals("14:30", TimeParser.formatTime(14.5));
        assertEquals("18:15", TimeParser.formatTime(18.25));
        assertEquals("00:45", TimeParser.formatTime(0.75));
    }
}