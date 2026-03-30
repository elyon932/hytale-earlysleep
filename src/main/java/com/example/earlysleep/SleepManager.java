package com.example.earlysleep;

import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSlumber;
import com.hypixel.hytale.builtin.beds.sleep.resources.WorldSomnolence;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.asset.type.gameplay.sleep.SleepConfig;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SleepManager {
   public double sleepStart = (double)19.5F;
   public double wakeUpTime = (double)5.5F;
   public String sleepThreshold = "50%";
   public long sleepDelay = -1L;
   private final Map<World, Integer> lastWorldSleepingCount = new ConcurrentHashMap();
   public boolean loadMessageEnabled = true;
   public boolean sleepEffectsEnabled = true;
   private final Map<UUID, Class<?>> lastState = new ConcurrentHashMap();
   private final File configFile = new File("plugins/EarlySleep/config.json");
   private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

   public SleepManager() {
   }

   public void init() {
      this.loadConfig();
      this.scheduler.scheduleAtFixedRate(this::checkSleepCycles, 10L, 200L, TimeUnit.MILLISECONDS);
      this.scheduler.scheduleAtFixedRate(this::modifyActiveWorldSleepConfigs, 10L, 15L, TimeUnit.SECONDS);
   }

   private void loadConfig() {
      if (!this.configFile.exists()) {
         this.saveConfig((double)19.5F, (double)5.5F);
      } else {
         try {
            String content = new String(Files.readAllBytes(this.configFile.toPath()));
            this.sleepStart = Double.parseDouble(content.split("\"sleepStart\":")[1].split(",")[0].trim());
            this.wakeUpTime = Double.parseDouble(content.split("\"wakeUpTime\":")[1].split(",")[0].trim());
            if (content.contains("\"sleepThreshold\":")) {
               this.sleepThreshold = content.split("\"sleepThreshold\":")[1].split("\"")[1].trim();
            }

            if (content.contains("\"sleepDelay\":")) {
               this.sleepDelay = Long.parseLong(content.split("\"sleepDelay\":")[1].split(",")[0].split("}")[0].trim());
            }

            if (content.contains("\"loadMessageEnabled\":")) {
               this.loadMessageEnabled = Boolean.parseBoolean(content.split("\"loadMessageEnabled\":")[1].split(",")[0].split("}")[0].trim());
            }

            if (content.contains("\"sleepEffectsEnabled\":")) {
               this.sleepEffectsEnabled = Boolean.parseBoolean(content.split("\"sleepEffectsEnabled\":")[1].split(",")[0].split("}")[0].trim());
            }
         } catch (Exception var2) {
            System.err.println("[EarlySleep] Failed to load config.json, using defaults.");
         }
      }

   }

   public void saveConfig(double start, double wake) {
      if (start >= (double)0.0F) {
         this.sleepStart = start;
      }

      if (wake >= (double)0.0F) {
         this.wakeUpTime = wake;
      }

      try {
         if (!this.configFile.getParentFile().exists()) {
            this.configFile.getParentFile().mkdirs();
         }

         String json = "{\n  \"sleepStart\": " + this.sleepStart + ",\n  \"wakeUpTime\": " + this.wakeUpTime + ",\n  \"sleepThreshold\": \"" + this.sleepThreshold + "\",\n  \"sleepDelay\": " + this.sleepDelay + ",\n  \"loadMessageEnabled\": " + this.loadMessageEnabled + ",\n  \"sleepEffectsEnabled\": " + this.sleepEffectsEnabled + "\n}";
         Files.write(this.configFile.toPath(), json.getBytes(), new OpenOption[0]);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void modifyActiveWorldSleepConfigs() {
      try {
         Field rangeField = SleepConfig.class.getDeclaredField("allowedSleepHoursRange");
         Field wakeField = SleepConfig.class.getDeclaredField("wakeUpHour");
         rangeField.setAccessible(true);
         wakeField.setAccessible(true);
         double[] range = new double[]{this.sleepStart, this.wakeUpTime};
         Universe universe = Universe.get();
         if (universe == null) {
            return;
         }

         for(World world : universe.getWorlds().values()) {
            SleepConfig config = world.getGameplayConfig().getWorldConfig().getSleepConfig();
            if (config != null) {
               rangeField.set(config, range);
               wakeField.set(config, (float)this.wakeUpTime);
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
        if (entityStore == null) return;
        Store<EntityStore> store = entityStore.getStore();

        for (PlayerRef p : world.getPlayerRefs()) {
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
                PlayerSomnolence som = (PlayerSomnolence) store.getComponent(ref, PlayerSomnolence.getComponentType());
                if (som != null && som.getSleepState() != null) {
                    Class<?> currentClass = som.getSleepState().getClass();
                    Class<?> previousClass = this.lastState.put(p.getUuid(), currentClass);

                    if (this.sleepEffectsEnabled && previousClass != null && previousClass != currentClass && currentClass == PlayerSleep.MorningWakeUp.class) {
                        this.applySleepBuffs(ref, store);
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
        if (entityStore == null) return;
        Store<EntityStore> store = entityStore.getStore();
        
        WorldTimeResource timeRes = (WorldTimeResource) store.getResource(WorldTimeResource.getResourceType());
        if (timeRes == null) return;

        LocalDateTime now = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC);
        double currentHour = (double) now.getHour() + (double) now.getMinute() / 60.0;
        
        // Bloqueio de horário: Só permite o processamento do sono se estiver no horário configurado
        boolean isNightTime = currentHour >= this.sleepStart || currentHour < this.wakeUpTime;
        if (!isNightTime) return;

        WorldSomnolence worldSom = (WorldSomnolence) store.getResource(WorldSomnolence.getResourceType());
        if (worldSom == null || worldSom.getState() instanceof WorldSlumber) return;

        Collection<PlayerRef> players = world.getPlayerRefs();
        int totalOnline = players.size();
        if (totalOnline == 0) return;

        int sleepingCount = 0;
        StringBuilder awakePlayers = new StringBuilder();
        long currentDelay = this.sleepDelay == -1L ? (this.getGlobalPlayerCount() == 1 ? 4000L : 0L) : this.sleepDelay;

        for (PlayerRef p : players) {
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
                PlayerSomnolence som = (PlayerSomnolence) store.getComponent(ref, PlayerSomnolence.getComponentType());
                if (som != null && som.getSleepState() != null) {
                    boolean isSleeping = false;
                    if (som.getSleepState() instanceof PlayerSleep.Slumber) {
                        isSleeping = true;
                    } else if (som.getSleepState() instanceof PlayerSleep.NoddingOff) {
                        PlayerSleep.NoddingOff nodding = (PlayerSleep.NoddingOff) som.getSleepState();
                        if (Instant.now().isAfter(nodding.realTimeStart().plusMillis(currentDelay))) {
                            isSleeping = true;
                        }
                    }

                    if (isSleeping) {
                        ++sleepingCount;
                    } else {
                        if (awakePlayers.length() > 0) awakePlayers.append(", ");
                        awakePlayers.append(p.getUsername());
                    }
                }
            }
        }

        int required = this.sleepThreshold.endsWith("%") ? (int) Math.ceil((double) (Integer.parseInt(this.sleepThreshold.replace("%", "")) * totalOnline) / 100.0) : Integer.parseInt(this.sleepThreshold);
        int lastCount = this.lastWorldSleepingCount.getOrDefault(world, 0);
        
        if (totalOnline > 1 && sleepingCount > lastCount) {
            SleepManagerCommand.broadcastSleepStatus(players, sleepingCount, required, awakePlayers.toString());
        }
        
        this.lastWorldSleepingCount.put(world, sleepingCount);
        
        if (sleepingCount >= required && sleepingCount > 0) {
            this.triggerSlumber(store, world);
        }
    } catch (Exception e) {
        e.printStackTrace(System.err);
    }
}

   private void applySleepBuffs(Ref<EntityStore> ref, Store<EntityStore> store) {
      EntityStatMap stats = (EntityStatMap)store.getComponent(ref, EntityStatMap.getComponentType());
      if (stats != null) {
         stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
         stats.maximizeStatValue(DefaultEntityStatTypes.getStamina());
      }

      EffectControllerComponent effectController = (EffectControllerComponent)store.getComponent(ref, EffectControllerComponent.getComponentType());
      if (effectController != null) {
         EntityEffect healthRegen = (EntityEffect)EntityEffect.getAssetMap().getAsset("Food_Health_Regen_Small");
         if (healthRegen != null) {
            effectController.addEffect(ref, healthRegen, 15.0F, OverlapBehavior.OVERWRITE, store);
         }

         EntityEffect staminaRegen = (EntityEffect)EntityEffect.getAssetMap().getAsset("Potion_Stamina_Regen");
         if (staminaRegen != null) {
            effectController.addEffect(ref, staminaRegen, 5.0F, OverlapBehavior.OVERWRITE, store);
         }
      }
   }

   private void triggerSlumber(Store<EntityStore> store, World world) {
      WorldTimeResource timeRes = (WorldTimeResource)store.getResource(WorldTimeResource.getResourceType());
      if (timeRes != null) {
         System.err.println("[EarlySleep] Falha ao disparar Slumber: WorldTimeResource é nulo.");
         Instant wakeUpInstant = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC).toLocalDate().plusDays(1L).atTime((int)this.wakeUpTime, (int)(this.wakeUpTime % (double)1.0F * (double)60.0F)).toInstant(ZoneOffset.UTC);
         timeRes.setGameTime(wakeUpInstant, world, store);

         for(PlayerRef p : world.getPlayerRefs()) {
            Ref<EntityStore> ref = p.getReference();
            if (ref != null) {
               PlayerSomnolence som = (PlayerSomnolence)store.getComponent(ref, PlayerSomnolence.getComponentType());
               if (som != null && (som.getSleepState() instanceof PlayerSleep.NoddingOff || som.getSleepState() instanceof PlayerSleep.Slumber)) {
                  store.putComponent(ref, PlayerSomnolence.getComponentType(), new PlayerSomnolence(new PlayerSleep.MorningWakeUp(wakeUpInstant)));
               }
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

         for(World w : u.getWorlds().values()) {
            count += w.getPlayerRefs().size();
         }

         return Math.max(1, count);
      }
   }
}
