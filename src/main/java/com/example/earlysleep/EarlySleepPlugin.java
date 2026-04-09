package com.example.earlysleep;

import com.example.earlysleep.command.SleepCommand;
import com.example.earlysleep.config.SleepConfig;
import com.example.earlysleep.service.BuffEffectService;
import com.example.earlysleep.service.HealthRecoveryService;
import com.example.earlysleep.service.SleepManager;
import com.example.earlysleep.util.ChatUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;

import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EarlySleepPlugin extends JavaPlugin {
    private SleepManager sleepManager;
    private SleepConfig sleepConfig;

    public EarlySleepPlugin(JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        this.sleepConfig = new SleepConfig();
        HealthRecoveryService healthRecoveryService = new HealthRecoveryService();
        BuffEffectService buffEffectService = new BuffEffectService();

        this.sleepManager = new SleepManager(this.sleepConfig, healthRecoveryService, buffEffectService);
        this.sleepManager.init();

        this.getCommandRegistry().registerCommand(new SleepCommand(this.sleepManager, this.sleepConfig));

        // Delay load message to ensure players/worlds are fully initialized
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (this.sleepConfig.loadMessageEnabled) {
                Universe universe = Universe.get();
                if (universe != null) {
                    Message msg1 = ChatUtil.createMessage("[EarlySleep] Loaded Successfully.", Color.CYAN);
                    Message msg2 = ChatUtil.createMessage("[EarlySleep] New commands available: use /sm help", Color.GREEN);
                    universe.getWorlds().values().forEach((world) -> world.getPlayerRefs().forEach((playerRef) -> {
                        playerRef.sendMessage(msg1);
                        playerRef.sendMessage(msg2);
                    }));
                }
            }
        }, 25L, TimeUnit.SECONDS);
    }
}