package com.example.earlysleep;

import com.example.earlysleep.command.EarlySleepCommandRouter;
import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.core.PlayerRecoveryService;
import com.example.earlysleep.core.SleepCycleProcessor;
import com.example.earlysleep.core.TimeTransitionEngine;
import com.example.earlysleep.core.WorldConfigSynchronizer;
import com.example.earlysleep.task.SchedulerManager;
import com.example.earlysleep.util.LoggerFacade;
import com.example.earlysleep.util.MessagingService;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the EarlySleep mod.
 * Initializes core services, registers commands, and schedules background tasks.
 */
public class EarlySleepPlugin extends JavaPlugin {

   private final ConfigManager configManager;
   private final SchedulerManager schedulerManager;

   public EarlySleepPlugin(JavaPluginInit init) {
      super(init);
      this.configManager = new ConfigManager();
      this.schedulerManager = new SchedulerManager();
   }

   @Override
   protected void setup() {
      LoggerFacade.info("Initializing EarlySleep modules...");
      
      configManager.loadConfiguration();

      // Initialize core services
      PlayerRecoveryService recoveryService = new PlayerRecoveryService();
      TimeTransitionEngine transitionEngine = new TimeTransitionEngine(configManager);
      WorldConfigSynchronizer configSynchronizer = new WorldConfigSynchronizer(configManager);
      
      SleepCycleProcessor sleepCycleProcessor = new SleepCycleProcessor(
            configManager, 
            recoveryService, 
            transitionEngine
      );

      // Register Command Router
      this.getCommandRegistry().registerCommand(
            new EarlySleepCommandRouter(configManager, configSynchronizer)
      );

      // Schedule fixed-rate tasks
      schedulerManager.scheduleAtFixedRate(sleepCycleProcessor::processTicks, 10L, 200L, TimeUnit.MILLISECONDS);
      schedulerManager.scheduleAtFixedRate(configSynchronizer::synchronizeActiveWorlds, 10L, 15L, TimeUnit.SECONDS);

      // Schedule welcome message
      scheduleWelcomeMessage();
      
      LoggerFacade.info("EarlySleep initialization complete.");
   }

   private void scheduleWelcomeMessage() {
      schedulerManager.schedule(() -> {
         if (configManager.getConfig().isLoadMessageEnabled()) {
            Universe universe = Universe.get();
            if (universe != null) {
               universe.getWorlds().values().forEach(world -> 
                  world.getPlayerRefs().forEach(MessagingService::sendWelcomeMessages)
               );
            }
         }
      }, 25L, TimeUnit.SECONDS);
   }
}