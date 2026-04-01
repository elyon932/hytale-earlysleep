package com.example.earlysleep.command;

import com.example.earlysleep.command.handler.CommandHandler;
import com.example.earlysleep.command.handler.DelaySettingHandler;
import com.example.earlysleep.command.handler.HelpMenuHandler;
import com.example.earlysleep.command.handler.StatusReportHandler;
import com.example.earlysleep.command.handler.ThresholdSettingHandler;
import com.example.earlysleep.command.handler.TimeSettingHandler;
import com.example.earlysleep.command.handler.ToggleFeatureHandler;
import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.core.WorldConfigSynchronizer;
import com.example.earlysleep.util.MessagingService;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Command Router utilizing the Strategy Pattern to distribute 
 * execution of sub-commands to specific handlers.
 */
public class EarlySleepCommandRouter extends CommandBase {

   private static final String PERMISSION_ADMIN = "earlysleep.admin";
   private final Map<String, CommandHandler> handlers = new HashMap<>();

   public EarlySleepCommandRouter(ConfigManager configManager, WorldConfigSynchronizer synchronizer) {
      super("sm", "Manages sleep schedule and requirements", false);
      this.setAllowsExtraArguments(true);
      
      registerHandlers(configManager, synchronizer);
   }

   private void registerHandlers(ConfigManager configManager, WorldConfigSynchronizer synchronizer) {
      TimeSettingHandler timeHandler = new TimeSettingHandler(configManager, synchronizer);
      ToggleFeatureHandler toggleHandler = new ToggleFeatureHandler(configManager);

      handlers.put("sleep", timeHandler);
      handlers.put("wake", timeHandler);
      handlers.put("delay", new DelaySettingHandler(configManager));
      handlers.put("player", new ThresholdSettingHandler(configManager));
      handlers.put("loadmsg", toggleHandler);
      handlers.put("effects", toggleHandler);
      handlers.put("status", new StatusReportHandler(configManager));
      handlers.put("help", new HelpMenuHandler());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.split("\\s+");

      if (!context.sender().hasPermission(PERMISSION_ADMIN)) {
         MessagingService.sendError(context, "Permission denied.");
         return;
      }

      if (parts.length < 2) {
         sendUsage(context, "sm");
         return;
      }

      CommandHandler handler = handlers.get(parts[1]);
      if (handler != null) {
         handler.execute(context, parts);
      } else {
         sendUsage(context, "sm");
      }
   }

   private void sendUsage(CommandContext context, String label) {
      String msg = switch (label) {
         case "player" -> "/sm player <value>";
         case "loadmsg" -> "/sm loadmsg <on/off>";
         case "effects" -> "/sm effects <on/off>";
         default -> "/sm help";
      };
      MessagingService.sendError(context, "Usage: " + msg);
   }
}