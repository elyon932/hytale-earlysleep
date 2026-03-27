package com.example.earlysleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
   private final SleepManager sleepManager = new SleepManager();

   public Main(JavaPluginInit init) {
      super(init);
   }

   protected void setup() {
      this.sleepManager.init();
      this.getCommandRegistry().registerCommand(new SleepManagerCommand(this.sleepManager));
      Executors.newSingleThreadScheduledExecutor().schedule(() -> {
         if (this.sleepManager.loadMessageEnabled) {
            Universe universe = Universe.get();
            if (universe != null) {
               Message msg1 = Message.raw("[EarlySleep] Loaded Successfully.").color(Color.CYAN);
               Message msg2 = Message.raw("[EarlySleep] New commands available: use /sm help").color(Color.GREEN);
               universe.getWorlds().values().forEach((world) -> world.getPlayerRefs().forEach((playerRef) -> {
                     playerRef.sendMessage(msg1);
                     playerRef.sendMessage(msg2);
                  }));
            }
         }
      }, 25L, TimeUnit.SECONDS);
   }
}
