package com.example.earlysleep.util;

/**
 * Simple facade to standardize error logging across the plugin cleanly.
 */
public final class LoggerFacade {

   private static final String PREFIX = "[EarlySleep] ";

   private LoggerFacade() {
      // Prevent instantiation
   }

   public static void info(String message) {
      System.out.println(PREFIX + message);
   }

   public static void error(String message) {
      System.err.println(PREFIX + "ERROR: " + message);
   }

   public static void error(String message, Throwable t) {
      System.err.println(PREFIX + "CRITICAL: " + message);
      t.printStackTrace(System.err);
   }
}