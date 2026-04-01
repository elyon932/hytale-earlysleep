package com.example.earlysleep.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.awt.Color;
import java.util.Collection;

/**
 * Standardizes outbound messages sent directly to clients or command context.
 */
public final class MessagingService {

   private static final String PREFIX = "[EarlySleep] ";

   private MessagingService() {
      // Prevent instantiation
   }

   public static void sendWelcomeMessages(PlayerRef player) {
      player.sendMessage(Message.raw(PREFIX + "Loaded Successfully.").color(Color.CYAN));
      player.sendMessage(Message.raw(PREFIX + "New commands available: use /sm help").color(Color.GREEN));
   }

   public static void sendError(CommandContext context, String content) {
      context.sendMessage(Message.raw(PREFIX + content).color(Color.RED));
   }

   public static void sendSuccess(CommandContext context, String content) {
      context.sendMessage(Message.raw(PREFIX + content).color(Color.GREEN));
   }

   public static void sendInfo(CommandContext context, String content) {
      context.sendMessage(Message.raw(PREFIX + content).color(Color.YELLOW));
   }

   public static void broadcastSleepStatus(Collection<PlayerRef> players, int current, int required, String awakeNames) {
      Message msg;
      if (current < required) {
         int missing = required - current;
         String detail = String.format("%d/%d players sleeping. (Missing: %d | Awake: %s)", 
               current, required, missing, awakeNames);
         msg = Message.raw(PREFIX + detail).color(Color.YELLOW);
      } else {
         msg = Message.raw(PREFIX + "Requirement met (" + required + " players). Night skip in progress...").color(Color.GREEN);
      }
      players.forEach(p -> p.sendMessage(msg));
   }
}