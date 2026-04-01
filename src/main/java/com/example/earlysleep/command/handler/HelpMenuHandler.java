package com.example.earlysleep.command.handler;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import java.awt.Color;

public class HelpMenuHandler implements CommandHandler {

   @Override
   public void execute(CommandContext context, String[] parts) {
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
}