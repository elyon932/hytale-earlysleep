package com.example.earlysleep.util;

/**
 * Utility class dedicated to formatting and parsing time structures safely.
 */
public final class TimeFormatter {

   private TimeFormatter() {
      // Prevent instantiation
   }

   /**
    * Parses standard human time input into engine-compatible double representation.
    * Allows both absolute integers (0-23) and specific formatted structures (HH:mm).
    *
    * @param s The raw string input from the user
    * @return Double mapping to engine time, or -1.0 on failure
    */
   public static double parseTimeToDouble(String s) {
      try {
         if (s.matches("^(0?[0-9]|1[0-9]|2[0-3])$")) {
            return Double.parseDouble(s);
         }

         if (s.matches("^(0?[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
            String[] parts = s.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return (double) hours + ((double) minutes / 60.0);
         }

         return -1.0;
      } catch (Exception e) {
         return -1.0;
      }
   }

   /**
    * Converts double-based engine time into human-readable formatted string (HH:mm).
    *
    * @param hours The engine time mapping
    * @return A formatted String
    */
   public static String formatDoubleToTime(double hours) {
      int h = (int) hours;
      int m = (int) ((hours % 1.0) * 60.0 + 0.5);
      return String.format("%02d:%02d", h, m);
   }
}