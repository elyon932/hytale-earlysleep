package com.example.earlysleep.command.handler;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.MessagingService;
import com.example.earlysleep.util.ThresholdCalculator;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class StatusReportHandler implements CommandHandler {

   private final ConfigManager configManager;

   public StatusReportHandler(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @Override
   public void execute(CommandContext context, String[] parts) {
      int online = ThresholdCalculator.getGlobalPlayerCount();
      String threshold = configManager.getConfig().getSleepThreshold();
      
      int displayAbs;
      int displayPct;

      if (threshold.endsWith("%")) {
         displayPct = Integer.parseInt(threshold.replace("%", ""));
         displayAbs = ThresholdCalculator.calculateRequiredPlayers(threshold, online);
      } else {
         displayAbs = Integer.parseInt(threshold);
         displayPct = online > 0 ? (displayAbs * 100) / online : 100;
      }

      String effectsState = configManager.getConfig().isSleepEffectsEnabled() ? "ON" : "OFF";
      String msg = String.format("Current threshold: %d%% (%d players). Effects: %s", 
            displayPct, displayAbs, effectsState);
            
      MessagingService.sendInfo(context, msg);
   }
}