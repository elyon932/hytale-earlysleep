package com.example.earlysleep.command.handler;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.MessagingService;
import com.example.earlysleep.util.ThresholdCalculator;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class ThresholdSettingHandler implements CommandHandler {

   private final ConfigManager configManager;

   public ThresholdSettingHandler(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @Override
   public void execute(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         MessagingService.sendError(context, "Usage: /sm player <value/%>");
         return;
      }

      String raw = parts[2];
      int online = ThresholdCalculator.getGlobalPlayerCount();

      try {
         if (raw.endsWith("%")) {
            int pct = Integer.parseInt(raw.replace("%", ""));
            if (pct < 0 || pct > 100) {
               MessagingService.sendError(context, "Percentage must be between 0 and 100%.");
               return;
            }
         } else {
            int abs = Integer.parseInt(raw);
            if (abs < 1 || abs > online) {
               MessagingService.sendError(context, "Value must be between 1 and " + online + " (players online).");
               return;
            }
         }

         configManager.getConfig().setSleepThreshold(raw);
         configManager.saveConfiguration();
         MessagingService.sendSuccess(context, "Minimum requirement set to: " + raw);

      } catch (NumberFormatException e) {
         MessagingService.sendError(context, "Invalid format. Use numbers (e.g. 2) or percentage (e.g. 50%).");
      }
   }
}