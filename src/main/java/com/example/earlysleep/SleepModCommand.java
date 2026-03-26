package com.example.earlysleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.SleepConfig;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Locale;
import javax.annotation.Nonnull;

public class SleepModCommand extends CommandBase {
   public SleepModCommand(Main plugin, String name) {
      super(name, "Controls the server sleep schedule", false);
      this.setAllowsExtraArguments(true);
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
      if (!context.sender().hasPermission("earlysleep.admin")) {
         context.sendMessage(Message.raw("Permission denied: You must be OP.").color(Color.RED));
      } else if (parts.length >= 3 && parts[1].equals("set")) {
         String label = parts[0].replace("/", "");
         if (label.equals("sleeptime")) {
            this.handleSleepStart(context, parts[2]);
         } else if (label.equals("waketime")) {
            this.handleWakeTime(context, parts[2]);
         }

      } else {
         this.sendUsage(context);
      }
   }

   private void handleSleepStart(CommandContext context, String timeStr) {
      double hours = this.parseTimeToDouble(timeStr);
      if (hours < (double)0.0F) {
         context.sendMessage(Message.raw("Invalid format. Use HH:mm (e.g., 18:40)").color(Color.RED));
      } else {
         this.applyToWorlds(hours, (double)-1.0F);
         context.sendMessage(Message.raw("Sleep start time updated to: " + timeStr).color(Color.GREEN));
      }
   }

   private void handleWakeTime(CommandContext context, String timeStr) {
      double hours = this.parseTimeToDouble(timeStr);
      if (hours < (double)0.0F) {
         context.sendMessage(Message.raw("Invalid format. Use HH:mm (e.g., 05:23)").color(Color.RED));
      } else {
         this.applyToWorlds((double)-1.0F, hours);
         context.sendMessage(Message.raw("Wake up time updated to: " + timeStr).color(Color.CYAN));
      }
   }

   private void applyToWorlds(double startHour, double wakeHour) {
      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         Field wakeField = SleepConfig.class.getDeclaredField("wakeUpHour");
         rangeField.setAccessible(true);
         wakeField.setAccessible(true);

         for(World world : Universe.get().getWorlds().values()) {
            if (world != null) {
               SleepConfig config = world.getGameplayConfig().getWorldConfig().getSleepConfig();
               if (config != null) {
                  double[] range = (double[])rangeField.get(config);
                  if (startHour >= (double)0.0F) {
                     range[0] = startHour;
                  }

                  if (wakeHour >= (double)0.0F) {
                     range[1] = wakeHour;
                     wakeField.set(config, (float)wakeHour);
                  }

                  rangeField.set(config, range);
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
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

   private void sendUsage(CommandContext context) {
      context.sendMessage(Message.raw("Usage: /sleeptime set <HH:mm> OR /waketime set <HH:mm>").color(Color.YELLOW));
   }
}
