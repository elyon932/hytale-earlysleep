package com.example.earlysleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.SleepConfig;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
   public double sleepStart = 19.0D;
   public double wakeUpTime = 5.5D;
   public String sleepThreshold = "50%";
   public long sleepDelay = -1L;
   private final File configFile = new File("plugins/EarlySleep/config.json");

   public Main(JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      this.loadConfig();
      this.getCommandRegistry().registerCommand(new SleepManagerCommand(this));
      Executors.newSingleThreadScheduledExecutor().schedule(() -> {
         Universe universe = Universe.get();
         if (universe != null) {
            Message msg1 = Message.raw("[Sleep Manager] Loaded Sucessfully.").color(Color.CYAN);
            Message msg2 = Message.raw("[Sleep Manager] New commands available: use /sm help to see").color(Color.GREEN);
            universe.getWorlds().values().forEach((world) -> {
               world.getPlayerRefs().forEach((playerRef) -> {
                  playerRef.sendMessage(msg1);
                  playerRef.sendMessage(msg2);
               });
            });
         }
      }, 10L, TimeUnit.SECONDS);
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::modifyActiveWorldSleepConfigs, 10L, 15L, TimeUnit.SECONDS);
   }

   private void loadConfig() {
      if (!this.configFile.exists()) {
         this.saveConfig(19.5D, 5.5D);
      } else {
         try {
            String content = new String(Files.readAllBytes(this.configFile.toPath()));
            this.sleepStart = Double.parseDouble(content.split("\"sleepStart\":")[1].split(",")[0].trim());
            this.wakeUpTime = Double.parseDouble(content.split("\"wakeUpTime\":")[1].split(",")[0].trim());
            if (content.contains("\"sleepThreshold\":")) {
               this.sleepThreshold = content.split("\"sleepThreshold\":")[1].split("\"")[1].trim();
            }

            if (content.contains("\"sleepDelay\":")) {
               this.sleepDelay = Long.parseLong(content.split("\"sleepDelay\":")[1].split(",")[0].split("}")[0].trim());
            }
         } catch (Exception var2) {
            System.err.println("[EarlySleep] Failed to load config.json, using defaults.");
         }
      }

   }

   public void saveConfig(double start, double wake) {
      if (start >= 0.0D) {
         this.sleepStart = start;
      }

      if (wake >= 0.0D) {
         this.wakeUpTime = wake;
      }

      try {
         if (!this.configFile.getParentFile().exists()) {
            this.configFile.getParentFile().mkdirs();
         }

         String json = "{\n  \"sleepStart\": " + this.sleepStart + ",\n  \"wakeUpTime\": " + this.wakeUpTime + ",\n  \"sleepThreshold\": \"" + this.sleepThreshold + "\",\n  \"sleepDelay\": " + this.sleepDelay + "\n}";
         Files.write(this.configFile.toPath(), json.getBytes(), new OpenOption[0]);
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   public void modifyActiveWorldSleepConfigs() {
      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         Field wakeField = SleepConfig.class.getDeclaredField("wakeUpHour");
         rangeField.setAccessible(true);
         wakeField.setAccessible(true);
         double[] range = new double[]{this.sleepStart, this.wakeUpTime};
         Iterator var4 = Universe.get().getWorlds().values().iterator();

         while(var4.hasNext()) {
            World world = (World)var4.next();
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