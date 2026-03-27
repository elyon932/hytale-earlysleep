package com.example.earlysleep;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.awt.Color;
import java.util.Collection;
import java.util.Locale;
import javax.annotation.Nonnull;

public class SleepManagerCommand extends CommandBase {
   private final SleepManager manager;

   public SleepManagerCommand(SleepManager manager) {
      super("sm", "Manages sleep schedule and requirements", false);
      this.manager = manager;
      this.setAllowsExtraArguments(true);
   }

   protected void executeSync(@Nonnull CommandContext context) {
      String input = context.getInputString();
      String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
      String[] parts = normalized.split("\\s+");
      if (!context.sender().hasPermission("earlysleep.admin")) {
         context.sendMessage(Message.raw("[EarlySleep] Permission denied.").color(Color.RED));
      } else if (parts.length < 2) {
         this.sendUsage(context, "sm");
      } else {
         switch (parts[1]) {
            case "sleep":
               if (parts.length >= 3) {
                  this.handleTimeChange(context, parts[2], true);
               } else {
                  this.sendUsage(context, "sm");
               }
               break;
            case "wake":
               if (parts.length >= 3) {
                  this.handleTimeChange(context, parts[2], false);
               } else {
                  this.sendUsage(context, "sm");
               }
               break;
            case "delay":
               this.handleDelay(context, parts);
               break;
            case "player":
               this.handleSet(context, parts);
               break;
            case "loadmsg":
               this.handleToggle(context, parts, "loadMessage");
               break;
            case "effects":
               this.handleToggle(context, parts, "effects");
               break;
            case "status":
               this.handleGet(context);
               break;
            case "help":
               this.handleHelp(context);
               break;
            default:
               this.sendUsage(context, "sm");
         }

      }
   }

   private void handleToggle(CommandContext context, String[] parts, String type) {
      if (parts.length < 3) {
         this.sendUsage(context, parts[1]);
      } else {
         boolean val = parts[2].equals("on");
         if (type.equals("loadMessage")) {
            this.manager.loadMessageEnabled = val;
         } else {
            this.manager.sleepEffectsEnabled = val;
         }

         this.manager.saveConfig((double)-1.0F, (double)-1.0F);
         String state = val ? "ENABLED" : "DISABLED";
         String var10001 = parts[1].toUpperCase();
         context.sendMessage(Message.raw("[EarlySleep] " + var10001 + " is now: " + state).color(val ? Color.GREEN : Color.RED));
      }
   }

   private void handleDelay(CommandContext context, String[] parts) {
      if (parts.length >= 3 && parts[2].equalsIgnoreCase("status")) {
         long current = this.manager.sleepDelay == -1L ? (this.manager.getGlobalPlayerCount() == 1 ? 4000L : 0L) : this.manager.sleepDelay;
         String mode = this.manager.sleepDelay == -1L ? " (Auto)" : "";
         context.sendMessage(Message.raw("[EarlySleep] Current sleep delay: " + current + "ms" + mode).color(Color.YELLOW));
      } else if (parts.length < 3) {
         this.sendUsage(context, "delay");
      } else {
         try {
            long value = Long.parseLong(parts[2]);
            if (value < 0L || value > 4000L) {
               context.sendMessage(Message.raw("[EarlySleep] Delay must be between 0 and 4000ms.").color(Color.RED));
               return;
            }

            this.manager.sleepDelay = value;
            this.manager.saveConfig((double)-1.0F, (double)-1.0F);
            context.sendMessage(Message.raw("[EarlySleep] Sleep delay set to: " + value + "ms").color(Color.GREEN));
         } catch (NumberFormatException var6) {
            context.sendMessage(Message.raw("[EarlySleep] Invalid number format.").color(Color.RED));
         }

      }
   }

   private void handleTimeChange(CommandContext context, String timeStr, boolean isSleep) {
      double hours = this.parseTimeToDouble(timeStr);
      if (timeStr.equalsIgnoreCase("status")) {
         double val = isSleep ? this.manager.sleepStart : this.manager.wakeUpTime;
         String time = String.format("%02d:%02d", (int)val, (int)(val % (double)1.0F * (double)60.0F));
         String type = isSleep ? "sleep start" : "wake up";
         context.sendMessage(Message.raw("[EarlySleep] Current " + type + " time: " + time).color(Color.YELLOW));
      } else {
         if (hours < (double)0.0F) {
            context.sendMessage(Message.raw("[EarlySleep] Invalid format (HH:mm).").color(Color.RED));
         } else {
            if (isSleep) {
               this.manager.saveConfig(hours, (double)-1.0F);
               context.sendMessage(Message.raw("[EarlySleep] Sleep time set to: " + timeStr).color(Color.GREEN));
            } else {
               this.manager.saveConfig((double)-1.0F, hours);
               context.sendMessage(Message.raw("[EarlySleep] Wake time set to: " + timeStr).color(Color.GREEN));
            }

            this.manager.modifyActiveWorldSleepConfigs();
         }

      }
   }

   private double parseTimeToDouble(String s) {
      try {
         String[] p = s.split(":");
         int h = Integer.parseInt(p[0]);
         int m = Integer.parseInt(p[1]);
         return h >= 0 && h <= 23 && m >= 0 && m <= 59 ? (double)h + (double)m / (double)60.0F : (double)-1.0F;
      } catch (Exception var5) {
         return (double)-1.0F;
      }
   }

   private void handleSet(CommandContext context, String[] parts) {
      if (parts.length < 3) {
         this.sendUsage(context, "player");
      } else {
         String raw = parts[2];
         this.manager.sleepThreshold = raw;
         this.manager.saveConfig((double)-1.0F, (double)-1.0F);
         context.sendMessage(Message.raw("[EarlySleep] Minimum requirement set to: " + raw).color(Color.GREEN));
      }
   }

   private void handleGet(CommandContext context) {
      int online = this.manager.getGlobalPlayerCount();
      String threshold = this.manager.sleepThreshold;
      int displayAbs;
      int displayPct;
      if (threshold.endsWith("%")) {
         displayPct = Integer.parseInt(threshold.replace("%", ""));
         displayAbs = (int)Math.ceil((double)(displayPct * online) / (double)100.0F);
      } else {
         displayAbs = Integer.parseInt(threshold);
         displayPct = online > 0 ? displayAbs * 100 / online : 100;
      }

      String msg = "[Early Sleep] Current threshold: " + displayPct + "% (" + displayAbs + " players). Effects: " + (this.manager.sleepEffectsEnabled ? "ON" : "OFF");
      context.sendMessage(Message.raw(msg).color(Color.YELLOW));
   }

   private void handleHelp(CommandContext context) {
      context.sendMessage(Message.raw("--- EarlySleep Command List ---").color(Color.CYAN));
      context.sendMessage(Message.raw("/sm sleep <HH:mm> - Set start sleep time.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm wake <HH:mm> - Set wake up time.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm player <val/%> - Set sleep requirements.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm delay <ms> - Set transition delay (0-4000).").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm loadmsg <on/off> - Toggle mod load message.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm effects <on/off> - Toggle heal/stamina on wake up.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm sleep status - Show current sleep start time.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm wake status - Show current wake up time.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm delay status - Show current transition delay.").color(Color.YELLOW));
      context.sendMessage(Message.raw("/sm status - Show current settings of minimum players.").color(Color.YELLOW));
   }

   public static void broadcastSleepStatus(Collection<PlayerRef> players, int current, int required, String awakeNames) {
      Message msg;
      if (current < required) {
         int missing = required - current;
         msg = Message.raw("[EarlySleep] " + current + "/" + required + " players sleeping. (Missing: " + missing + " | Awake: " + awakeNames + ")").color(Color.YELLOW);
      } else {
         msg = Message.raw("[EarlySleep] Requirement met (" + required + " players). Night skip in progress...").color(Color.GREEN);
      }

      players.forEach((p) -> p.sendMessage(msg));
   }

   private void sendUsage(CommandContext context, String label) {
      String msg = label.equals("player") ? "/sm player <value>" : (label.equals("loadmsg") ? "/sm loadmsg <on/off>" : (label.equals("effects") ? "/sm effects <on/off>" : "/sm help"));
      context.sendMessage(Message.raw("[EarlySleep] Usage: " + msg).color(Color.RED));
   }
}
