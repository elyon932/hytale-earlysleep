package com.example.earlysleep.service;

import com.example.earlysleep.config.SleepConfig;
import com.example.earlysleep.util.ChatUtil;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SleepManager {
    private final SleepConfig config;
    private final HealthRecoveryService healthRecoveryService;
    private final BuffEffectService buffEffectService;

    private final Map<World, Integer> lastWorldSleepingCount = new ConcurrentHashMap<>();
    private final Map<UUID, Class<?>> lastState = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public SleepManager(SleepConfig config, HealthRecoveryService healthRecoveryService, BuffEffectService buffEffectService) {
        this.config = config;
        this.healthRecoveryService = healthRecoveryService;
        this.buffEffectService = buffEffectService;
    }

    public void init() {
        this.config.load();
        this.scheduler.scheduleAtFixedRate(this::checkSleepCycles, 10L, 200L, TimeUnit.MILLISECONDS);
        this.scheduler.scheduleAtFixedRate(this::modifyActiveWorldSleepConfigs, 10L, 15L, TimeUnit.SECONDS);
    }

    public void modifyActiveWorldSleepConfigs() {
        try {
            Field rangeField = com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
            Field wakeField = com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig.class.getDeclaredField("wakeUpHour");
            rangeField.setAccessible(true);
            wakeField.setAccessible(true);
            double[] range = new double[]{this.config.sleepStart, this.config.wakeUpTime};
            Universe universe = Universe.get();
            if (universe == null) {
                return;
            }

            for (World world : universe.getWorlds().values()) {
                com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig worldConfig = world.getGameplayConfig().getWorldConfig().getSleepConfig();
                if (worldConfig != null) {
                    rangeField.set(worldConfig, range);
                    wakeField.set(worldConfig, (float)this.config.wakeUpTime);
                }
            }
        } catch (Exception var8) {
            System.err.println("[EarlySleep] Erro na Reflexão de SleepConfig: " + var8.getMessage());
            var8.printStackTrace(System.err);
        }
    }

    private void checkSleepCycles() {
        Universe universe = Universe.get();
        if (universe != null) {
            this.lastWorldSleepingCount.keySet().retainAll(universe.getWorlds().values());

            for (World world : universe.getWorlds().values()) {
                world.execute(() -> {
                    this.checkPlayerWakeUpEffects(world);
                    this.checkWorldSleep(world);
                });
            }
        }
    }

    private void checkPlayerWakeUpEffects(World world) {
        try {
            EntityStore entityStore = world.getEntityStore();
            Store<EntityStore> store = entityStore.getStore();

            for (PlayerRef p : world.getPlayerRefs()) {
                Ref<EntityStore> ref = p.getReference();
                if (ref != null) {
                    PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
                    if (som != null && som.getSleepState() != null) {
                        Class<?> currentClass = som.getSleepState().getClass();
                        Class<?> previousClass = this.lastState.put(p.getUuid(), currentClass);
                        if (this.config.sleepEffectsEnabled && previousClass != null && previousClass != currentClass && currentClass == PlayerSleep.MorningWakeUp.class) {
                            this.healthRecoveryService.recover(ref, store);
                            this.buffEffectService.applyBuffs(ref, store);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void checkWorldSleep(World world) {
        try {
            EntityStore entityStore = world.getEntityStore();
            Store<EntityStore> store = entityStore.getStore();
            WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
            LocalDateTime now = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC);
            double currentHour = (double)now.getHour() + (double)now.getMinute() / (double)60.0F;
            boolean isNightTime = currentHour >= this.config.sleepStart || currentHour < this.config.wakeUpTime;
            if (!isNightTime) {
                return;
            }

            WorldSomnolence worldSom = (WorldSomnolence)store.getResource(WorldSomnolence.getResourceType());
            if (worldSom == null || worldSom.getState() instanceof WorldSlumber) {
                return;
            }

            Collection<PlayerRef> players = world.getPlayerRefs();
            int totalOnline = players.size();
            if (totalOnline == 0) {
                return;
            }

            int sleepingCount = 0;
            StringBuilder awakePlayers = new StringBuilder();
            long currentDelay = this.config.sleepDelay == -1L ? (this.getGlobalPlayerCount() == 1 ? 4000L : 0L) : this.config.sleepDelay;

            for (PlayerRef p : players) {
                Ref<EntityStore> ref = p.getReference();
                if (ref != null) {
                    PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
                    if (som != null && som.getSleepState() != null) {
                        boolean isSleeping = false;
                        if (som.getSleepState() instanceof PlayerSleep.Slumber) {
                            isSleeping = true;
                        } else if (som.getSleepState() instanceof PlayerSleep.NoddingOff) {
                            PlayerSleep.NoddingOff nodding = (PlayerSleep.NoddingOff)som.getSleepState();
                            if (Instant.now().isAfter(nodding.realTimeStart().plusMillis(currentDelay))) {
                                isSleeping = true;
                            }
                        }

                        if (isSleeping) {
                            ++sleepingCount;
                        } else {
                            if (awakePlayers.length() > 0) {
                                awakePlayers.append(", ");
                            }
                            awakePlayers.append(p.getUsername());
                        }
                    }
                }
            }

            int required = this.config.sleepThreshold.endsWith("%") ? (int)Math.ceil((double)(Integer.parseInt(this.config.sleepThreshold.replace("%", "")) * totalOnline) / (double)100.0F) : Integer.parseInt(this.config.sleepThreshold);
            int lastCount = this.lastWorldSleepingCount.getOrDefault(world, 0);
            if (totalOnline > 1 && sleepingCount > lastCount) {
                ChatUtil.broadcastSleepStatus(players, sleepingCount, required, awakePlayers.toString());
            }

            this.lastWorldSleepingCount.put(world, sleepingCount);
            if (sleepingCount >= required && sleepingCount > 0) {
                this.triggerSlumber(store, world);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void triggerSlumber(Store<EntityStore> store, World world) {
        WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
        Instant wakeUpInstant = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC).toLocalDate().plusDays(1L).atTime((int)this.config.wakeUpTime, (int)(this.config.wakeUpTime % (double)1.0F * (double)60.0F)).toInstant(ZoneOffset.UTC);
        timeRes.setGameTime(wakeUpInstant, world, store);

        for (PlayerRef p : world.getPlayerRefs()) {
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
                PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
                if (som != null && (som.getSleepState() instanceof PlayerSleep.NoddingOff || som.getSleepState() instanceof PlayerSleep.Slumber)) {
                    store.putComponent(ref, PlayerSomnolence.getComponentType(), new PlayerSomnolence(new PlayerSleep.MorningWakeUp(wakeUpInstant)));
                }
            }
        }
    }

    public int getGlobalPlayerCount() {
        Universe u = Universe.get();
        if (u == null) {
            return 1;
        } else {
            int count = 0;
            for (World w : u.getWorlds().values()) {
                count += w.getPlayerRefs().size();
            }
            return Math.max(1, count);
        }
    }
}