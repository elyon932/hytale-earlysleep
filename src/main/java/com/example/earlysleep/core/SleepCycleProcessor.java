package com.example.earlysleep.core;

import com.example.earlysleep.config.ConfigManager;
import com.example.earlysleep.util.LoggerFacade;
import com.example.earlysleep.util.MessagingService;
import com.example.earlysleep.util.ThresholdCalculator;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core engine component that monitors player sleep states and triggers actions
 * based on environment rules and configuration thresholds.
 */
public class SleepCycleProcessor {

   private final ConfigManager configManager;
   private final PlayerRecoveryService recoveryService;
   private final TimeTransitionEngine transitionEngine;
   
   private final Map<World, Integer> lastWorldSleepingCount = new ConcurrentHashMap<>();
   private final Map<UUID, Class<?>> lastState = new ConcurrentHashMap<>();

   public SleepCycleProcessor(ConfigManager configManager, PlayerRecoveryService recoveryService, TimeTransitionEngine transitionEngine) {
      this.configManager = configManager;
      this.recoveryService = recoveryService;
      this.transitionEngine = transitionEngine;
   }

   /**
    * Executes the primary processing loop for all worlds within the universe.
    */
   public void processTicks() {
      Universe universe = Universe.get();
      if (universe != null) {
         lastWorldSleepingCount.keySet().retainAll(universe.getWorlds().values());
         for (World world : universe.getWorlds().values()) {
            world.execute(() -> {
               verifyPlayerWakeUpEffects(world);
               verifyWorldSleepRequirements(world);
            });
         }
      }
   }

   private void verifyPlayerWakeUpEffects(World world) {
      try {
         EntityStore entityStore = world.getEntityStore();
         Store<EntityStore> store = entityStore.getStore();

         for (PlayerRef p : world.getPlayerRefs()) {
            Ref<EntityStore> ref = p.getReference();
            if (ref == null) continue;

            PlayerSomnolence som = (PlayerSomnolence) store.getComponent(ref, PlayerSomnolence.getComponentType());
            if (som != null && som.getSleepState() != null) {
               Class<?> currentClass = som.getSleepState().getClass();
               Class<?> previousClass = lastState.put(p.getUuid(), currentClass);

               if (shouldApplyEffects(previousClass, currentClass)) {
                  recoveryService.applyWakeUpBuffs(ref, store);
               }
            }
         }
      } catch (Exception e) {
         LoggerFacade.error("Exception verifying player wake-up effects.", e);
      }
   }

   private boolean shouldApplyEffects(Class<?> previousClass, Class<?> currentClass) {
      return configManager.getConfig().isSleepEffectsEnabled() && 
             previousClass != null && 
             previousClass != currentClass && 
             currentClass == PlayerSleep.MorningWakeUp.class;
   }

   private void verifyWorldSleepRequirements(World world) {
      try {
         EntityStore entityStore = world.getEntityStore();
         Store<EntityStore> store = entityStore.getStore();

         WorldTimeResource timeRes = (WorldTimeResource) store.getResource(WorldTimeResource.getResourceType());
         if (timeRes == null || !isNightTime(timeRes)) return;

         WorldSomnolence worldSom = (WorldSomnolence) store.getResource(WorldSomnolence.getResourceType());
         if (worldSom == null || worldSom.getState() instanceof WorldSlumber) return;

         Collection<PlayerRef> players = world.getPlayerRefs();
         int totalOnline = players.size();
         if (totalOnline == 0) return;

         processPlayersSleepingState(world, store, players, totalOnline);

      } catch (Exception e) {
         LoggerFacade.error("Critical error in verifyWorldSleepRequirements: " + e.getMessage(), e);
      }
   }

   private void processPlayersSleepingState(World world, Store<EntityStore> store, Collection<PlayerRef> players, int totalOnline) {
      int sleepingCount = 0;
      StringBuilder awakePlayers = new StringBuilder();
      long currentDelay = calculateCurrentDelay();

      for (PlayerRef p : players) {
         Ref<EntityStore> ref = p.getReference();
         if (ref == null) continue;

         PlayerSomnolence som = (PlayerSomnolence) store.getComponent(ref, PlayerSomnolence.getComponentType());
         if (som != null && som.getSleepState() != null) {
            if (evaluateSleepingStatus(som, currentDelay)) {
               sleepingCount++;
            } else {
               if (awakePlayers.length() > 0) awakePlayers.append(", ");
               awakePlayers.append(p.getUsername());
            }
         }
      }

      int required = ThresholdCalculator.calculateRequiredPlayers(
            configManager.getConfig().getSleepThreshold(), totalOnline);
      int lastCount = lastWorldSleepingCount.getOrDefault(world, 0);

      if (totalOnline > 1 && sleepingCount > lastCount) {
         MessagingService.broadcastSleepStatus(players, sleepingCount, required, awakePlayers.toString());
      }

      lastWorldSleepingCount.put(world, sleepingCount);

      if (sleepingCount >= required && sleepingCount > 0) {
         transitionEngine.executeSlumberTransition(store, world);
      }
   }

   private boolean isNightTime(WorldTimeResource timeRes) {
      LocalDateTime now = LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC);
      double currentHour = now.getHour() + (now.getMinute() / 60.0);
      return currentHour >= configManager.getConfig().getSleepStart() || 
             currentHour < configManager.getConfig().getWakeUpTime();
   }

   private long calculateCurrentDelay() {
      long delay = configManager.getConfig().getSleepDelay();
      if (delay == -1L) {
         return ThresholdCalculator.getGlobalPlayerCount() == 1 ? 4000L : 0L;
      }
      return delay;
   }

   private boolean evaluateSleepingStatus(PlayerSomnolence som, long currentDelay) {
      if (som.getSleepState() instanceof PlayerSleep.Slumber) {
         return true;
      }
      if (som.getSleepState() instanceof PlayerSleep.NoddingOff) {
         PlayerSleep.NoddingOff nodding = (PlayerSleep.NoddingOff) som.getSleepState();
         return Instant.now().isAfter(nodding.realTimeStart().plusMillis(currentDelay));
      }
      return false;
   }
}