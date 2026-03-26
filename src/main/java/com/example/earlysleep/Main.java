package com.example.earlysleep;

import com.hypixel.hytale.server.core.asset.type.gameplay.SleepConfig;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
   public Main(JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      this.getCommandRegistry().registerCommand(new SleepModCommand(this, "sleeptime"));
      this.getCommandRegistry().registerCommand(new SleepModCommand(this, "waketime"));
      Executors.newSingleThreadScheduledExecutor().schedule(this::modifyActiveWorldSleepConfigs, 15L, TimeUnit.SECONDS);
      System.err.println("[EarlySleep] Setup complete. Commands registered.");
   }

   private void modifyActiveWorldSleepConfigs() {
      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         rangeField.setAccessible(true);
         double[] newRange = new double[]{(double)19.0F, (double)5.5F};

         for(World world : Universe.get().getWorlds().values()) {
            if (world != null) {
               SleepConfig config = world.getGameplayConfig().getWorldConfig().getSleepConfig();
               if (config != null) {
                  rangeField.set(config, newRange);
                  System.err.println("[EarlySleep] Initialized world: " + world.getName());
               }
            }
         }

         System.err.println("[EarlySleep] All worlds adjusted to 7:00 PM.");
      } catch (Exception e) {
         System.err.println("[EarlySleep] Error during boot scan.");
         e.printStackTrace();
      }

   }
}
