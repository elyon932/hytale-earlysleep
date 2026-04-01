package com.example.earlysleep.command.handler;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.core.WorldConfigSynchronizer;
import com.example.earlysleep.util.MessagingService;
import com.example.earlysleep.util.TimeFormatter;
import com.hypixel.hytale.server.core.command.system.CommandContext;

public class TimeSettingHandler implements CommandHandler {

   private final ConfigManager configManager;
   private final WorldConfigSynchronizer synchronizer;

   public TimeSettingHandler(ConfigManager configManager, WorldConfigSynchronizer synchronizer) {
      this.configManager = configManager;
      this.synchronizer = synchronizer;
   }

   @Override
   public void execute(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         MessagingService.sendError(context, "Usage: /sm " + parts[1] + " <HH:mm|Hour|status>");
         return;
      }

      boolean isSleep = parts[1].equals("sleep");
      String timeStr = parts[2];
      
      if (timeStr.equalsIgnoreCase("status")) {
         handleStatus(context, isSleep);
         return;
      }

      double hours = TimeFormatter.parseTimeToDouble(timeStr);
      if (hours < 0.0) {
         MessagingService.sendError(context, "Invalid format. Use HH:mm (e.g. 14:30) or Hour (0-23).");
         return;
      }

      applyTimeChange(context, isSleep, hours);
   }

   private void handleStatus(CommandContext context, boolean isSleep) {
      double val = isSleep ? configManager.getConfig().getSleepStart() : configManager.getConfig().getWakeUpTime();
      String time = TimeFormatter.formatDoubleToTime(val);
      String type = isSleep ? "sleep start" : "wake up";
      MessagingService.sendInfo(context, "Current " + type + " time: " + time);
   }

   private void applyTimeChange(CommandContext context, boolean isSleep, double hours) {
      String formattedTime = TimeFormatter.formatDoubleToTime(hours);
      
      if (isSleep) {
         configManager.getConfig().setSleepStart(hours);
         MessagingService.sendSuccess(context, "Sleep time set to: " + formattedTime);
      } else {
         configManager.getConfig().setWakeUpTime(hours);
         MessagingService.sendSuccess(context, "Wake time set to: " + formattedTime);
      }

      configManager.saveConfiguration();
      synchronizer.synchronizeActiveWorlds();
   }
}