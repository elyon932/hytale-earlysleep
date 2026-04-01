package com.example.earlysleep.core;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.LoggerFacade;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.lang.reflect.Field;

/**
 * Service responsible for mutating internal Hytale configurations via reflection
 * to ensure native sleeping mechanics align with mod settings.
 */
public class WorldConfigSynchronizer {

   private final ConfigManager configManager;

   public WorldConfigSynchronizer(ConfigManager configManager) {
      this.configManager = configManager;
   }

   /**
    * Synchronizes the active worlds' sleep configurations with the current mod settings.
    */
   public void synchronizeActiveWorlds() {
      Universe universe = Universe.get();
      if (universe == null) {
         return;
      }

      double sleepStart = configManager.getConfig().getSleepStart();
      double wakeUpTime = configManager.getConfig().getWakeUpTime();
      double[] range = new double[]{sleepStart, wakeUpTime};

      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         Field wakeField = SleepConfig.class.getDeclaredField("wakeUpHour");
         rangeField.setAccessible(true);
         wakeField.setAccessible(true);

         for (World world : universe.getWorlds().values()) {
            applyConfigToWorld(world, rangeField, wakeField, range, wakeUpTime);
         }
      } catch (Exception e) {
         LoggerFacade.error("Error during Reflection on SleepConfig: " + e.getMessage(), e);
      }
   }

   private void applyConfigToWorld(World world, Field rangeField, Field wakeField, double[] range, double wakeUpTime) throws IllegalAccessException {
      SleepConfig config = world.getGameplayConfig().getWorldConfig().getSleepConfig();
      if (config != null) {
         rangeField.set(config, range);
         wakeField.set(config, (float) wakeUpTime);
      }
   }
}