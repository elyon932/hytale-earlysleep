package com.example.earlysleep.util;

public class TimeParser {
    /**
     * Parses "HH" or "HH:mm" into decimal hours.
     *
     * @return parsed value, or -1 if invalid
     */
    public static double parseTimeToDouble(String s) {
        try {
            if (s.matches("^(0?[0-9]|1[0-9]|2[0-3])$")) {
                return Double.parseDouble(s);
            } else if (s.matches("^(0?[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
                String[] p = s.split(":");
                int h = Integer.parseInt(p[0]);
                int m = Integer.parseInt(p[1]);
                return (double)h + (double)m / (double)60.0F;
            } else {
                return (double)-1.0F;
            }
        } catch (Exception var5) {
            return (double)-1.0F;
        }
    }

    public static String formatTime(double hours) {
        return String.format("%02d:%02d", (int)hours, (int)(hours % (double)1.0F * (double)60.0F + (double)0.5F));
    }
}