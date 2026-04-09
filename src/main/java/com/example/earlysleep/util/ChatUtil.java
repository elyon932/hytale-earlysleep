package com.example.earlysleep.util;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.Color;
import java.util.Collection;

public class ChatUtil {
    public static Message createMessage(String text, Color color) {
        return Message.raw(text).color(color);
    }

    public static void broadcastSleepStatus(Collection<PlayerRef> players, int current, int required, String awakeNames) {
        Message msg;
        if (current < required) {
            int missing = required - current;
            msg = createMessage("[EarlySleep] " + current + "/" + required + " players sleeping. (Missing: " + missing + " | Awake: " + awakeNames + ")", Color.YELLOW);
        } else {
            msg = createMessage("[EarlySleep] Requirement met (" + required + " players). Night skip in progress...", Color.GREEN);
        }

        players.forEach((p) -> p.sendMessage(msg));
    }
}