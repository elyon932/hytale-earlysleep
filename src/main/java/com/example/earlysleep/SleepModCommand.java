package com.example.earlysleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import java.awt.Color;
import java.util.Locale;
import javax.annotation.Nonnull;

public class SleepModCommand extends CommandBase {
   private final Main plugin;

   public SleepModCommand(Main plugin, String name) {
      super(name, "Controls the server sleep schedule", false);
      this.plugin = plugin;
      this.setAllowsExtraArguments(true);
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
      String label = parts.length > 0 ? parts[0].replace("/", "") : "sleeptime";
      if (!context.sender().hasPermission("earlysleep.admin")) {
         context.sendMessage(Message.raw("Permission denied.").color(Color.RED));
      } else if (parts.length >= 3 && parts[1].equals("set")) {
         double hours = this.parseTimeToDouble(parts[2]);
         if (hours < (double)0.0F) {
            context.sendMessage(Message.raw("Invalid format (HH:mm).").color(Color.RED));
         } else {
            if (label.equals("sleeptime")) {
               this.plugin.saveConfig(hours, (double)-1.0F);
               context.sendMessage(Message.raw("Sleep time permanently set to: " + parts[2]).color(Color.GREEN));
            } else if (label.equals("waketime")) {
               this.plugin.saveConfig((double)-1.0F, hours);
               context.sendMessage(Message.raw("Wake time permanently set to: " + parts[2]).color(Color.GREEN));
            }

            this.plugin.modifyActiveWorldSleepConfigs();
         }
      } else {
         this.sendUsage(context, label);
      }

   }

   private double parseTimeToDouble(String timeStr) {
      try {
         String[] parts = timeStr.split(":");
         int h = Integer.parseInt(parts[0]);
         int m = Integer.parseInt(parts[1]);
         return h >= 0 && h <= 23 && m >= 0 && m <= 59 ? (double)h + (double)m / (double)60.0F : (double)-1.0F;
      } catch (Exception var5) {
         return (double)-1.0F;
      }
   }

   private void sendUsage(CommandContext context, String label) {
      String correctCmd = label.equals("waketime") ? "/waketime set <HH:mm>" : "/sleeptime set <HH:mm>";
      context.sendMessage(Message.raw("Usage: " + correctCmd).color(Color.RED));
   }
}
