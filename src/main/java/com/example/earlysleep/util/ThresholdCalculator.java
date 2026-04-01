package com.example.earlysleep.util;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;

/**
 * Utility responsible for calculating required online players based on configurations.
 */
public final class ThresholdCalculator {

   private ThresholdCalculator() {
      // Prevent instantiation
   }

   public static int getGlobalPlayerCount() {
      Universe universe = Universe.get();
      if (universe == null) {
         return 1;
      }

      int count = 0;
      for (World w : universe.getWorlds().values()) {
         count += w.getPlayerRefs().size();
      }
      return Math.max(1, count);
   }

   public static int calculateRequiredPlayers(String thresholdConfig, int totalOnline) {
      if (thresholdConfig.endsWith("%")) {
         int percentage = Integer.parseInt(thresholdConfig.replace("%", ""));
         return (int) Math.ceil((percentage * totalOnline) / 100.0);
      }
      return Integer.parseInt(thresholdConfig);
   }
}