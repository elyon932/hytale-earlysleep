package com.example.earlysleep.command.handler;

import com.hypixel.hytale.server.core.command.system.CommandContext;

/**
 * Strategy interface for individual command executions.
 */
public interface CommandHandler {
   /**
    * Executes the command logic based on the parsed input array.
    * * @param context Context of the command origin
    * @param parts   The argument segments of the command
    */
   void execute(CommandContext context, String[] parts);
}