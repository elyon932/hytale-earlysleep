package com.example.earlysleep.command.handler;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.MessagingService;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class ToggleFeatureHandler implements CommandHandler {

   private final ConfigManager configManager;

   public ToggleFeatureHandler(ConfigManager configManager) {
      this.configManager = configManager;
   }

   @Override
   public void execute(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         MessagingService.sendError(context, "Usage: /sm " + parts[1] + " <on/off>");
         return;
      }

      String input = parts[2].toLowerCase();
      if (!input.equals("on") && !input.equals("off") && !input.equals("true") && !input.equals("false")) {
         MessagingService.sendError(context, "Use 'on' or 'off'.");
         return;
      }

      boolean value = input.equals("on") || input.equals("true");
      String targetType = parts[1];

      if (targetType.equals("loadmsg")) {
         configManager.getConfig().setLoadMessageEnabled(value);
      } else {
         configManager.getConfig().setSleepEffectsEnabled(value);
      }

      configManager.saveConfiguration();
      
      String state = value ? "ENABLED" : "DISABLED";
      if (value) {
         MessagingService.sendSuccess(context, targetType.toUpperCase() + " is now: " + state);
      } else {
         MessagingService.sendError(context, targetType.toUpperCase() + " is now: " + state);
      }
   }
}