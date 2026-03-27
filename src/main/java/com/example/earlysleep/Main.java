package com.example.earlysleep;

import com.hypixel.hytale.server.core.asset.type.gameplay.SleepConfig;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
   private double sleepStart = (double)19.0F;
   private double wakeUpTime = (double)5.5F;
   private final File configFile = new File("plugins/EarlySleep/config.json");

   public Main(JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      this.loadConfig();
      this.getCommandRegistry().registerCommand(new SleepModCommand(this, "sleeptime"));
      this.getCommandRegistry().registerCommand(new SleepModCommand(this, "waketime"));
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::modifyActiveWorldSleepConfigs, 10L, 15L, TimeUnit.SECONDS);
      System.err.println("[EarlySleep] Plugin initialized with persistent config.");
   }

   private void loadConfig() {
      if (!this.configFile.exists()) {
         this.saveConfig((double)19.0F, (double)5.5F);
      } else {
         try {
            String content = new String(Files.readAllBytes(this.configFile.toPath()));
            this.sleepStart = Double.parseDouble(content.split("\"sleepStart\":")[1].split(",")[0].trim());
            this.wakeUpTime = Double.parseDouble(content.split("\"wakeUpTime\":")[1].split("}")[0].trim());
         } catch (Exception var2) {
            System.err.println("[EarlySleep] Failed to load config.json, using defaults.");
         }

      }
   }

   public void saveConfig(double start, double wake) {
      if (start >= (double)0.0F) {
         this.sleepStart = start;
      }

      if (wake >= (double)0.0F) {
         this.wakeUpTime = wake;
      }

      try {
         if (!this.configFile.getParentFile().exists()) {
            this.configFile.getParentFile().mkdirs();
         }

         String json = "{\n  \"sleepStart\": " + this.sleepStart + ",\n  \"wakeUpTime\": " + this.wakeUpTime + "\n}";
         Files.write(this.configFile.toPath(), json.getBytes(), new OpenOption[0]);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void modifyActiveWorldSleepConfigs() {
      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         Field wakeField = SleepConfig.class.getDeclaredField("wakeUpHour");
         rangeField.setAccessible(true);
         wakeField.setAccessible(true);
         double[] range = new double[]{this.sleepStart, this.wakeUpTime};

         for(World world : Universe.get().getWorlds().values()) {
            if (world != null) {
               SleepConfig config = world.getGameplayConfig().getWorldConfig().getSleepConfig();
               if (config != null) {
                  rangeField.set(config, range);
                  wakeField.set(config, (float)this.wakeUpTime);
               }
            }
         }
      } catch (Exception var7) {
      }

   }
}
