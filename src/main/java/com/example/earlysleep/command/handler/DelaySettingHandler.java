package com.example.earlysleep.command.handler;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.MessagingService;
import com.example.earlysleep.util.ThresholdCalculator;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class DelaySettingHandler implements CommandHandler {

   private final ConfigManager configManager;

   public DelaySettingHandler(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @Override
   public void execute(CommandContext context, String[] parts) {
      if (parts.length >= 3 && parts[2].equalsIgnoreCase("status")) {
         reportStatus(context);
      } else if (parts.length < 3) {
         MessagingService.sendError(context, "Usage: /sm delay <ms|status>");
      } else {
         applyDelay(context, parts[2]);
      }
   }

   private void reportStatus(CommandContext context) {
      long storedDelay = configManager.getConfig().getSleepDelay();
      long current = storedDelay == -1L ? 
            (ThresholdCalculator.getGlobalPlayerCount() == 1 ? 4000L : 0L) : storedDelay;
      String mode = storedDelay == -1L ? " (Auto)" : " (Manual)";
      
      MessagingService.sendInfo(context, "Current sleep delay: " + current + "ms" + mode);
   }

   private void applyDelay(CommandContext context, String rawValue) {
      try {
         long value = Long.parseLong(rawValue);
         if (value < 0L || value > 4000L) {
            MessagingService.sendError(context, "Delay must be between 0 and 4000ms.");
            return;
         }

         configManager.getConfig().setSleepDelay(value);
         configManager.saveConfiguration();
         MessagingService.sendSuccess(context, "Sleep delay set to: " + value + "ms");
         
      } catch (NumberFormatException e) {
         MessagingService.sendError(context, "Invalid number. Use a value between 0 and 4000.");
      }
   }
}