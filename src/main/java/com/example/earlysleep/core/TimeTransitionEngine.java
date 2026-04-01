package com.example.earlysleep.core;

import com.example.earlysleep.config.ConfigManager;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSleep;
import com.hypixel.hytale.builtin.beds.sleep.components.PlayerSomnolence;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Handles the calculation and execution of server time transitions (skipping the night).
 */
public class TimeTransitionEngine {

   private final ConfigManager configManager;

   public TimeTransitionEngine(ConfigManager configManager) {
      this.configManager = configManager;
   }

   /**
    * Forcibly advances the world time to the configured wake-up time 
    * and transitions sleeping players to the MorningWakeUp state.
    *
    * @param store The entity store containing world resources
    * @param world The target world
    */
   public void executeSlumberTransition(Store<EntityStore> store, World world) {
      WorldTimeResource timeRes = (WorldTimeResource) store.getResource(WorldTimeResource.getResourceType());
      Instant wakeUpInstant = calculateWakeUpInstant(timeRes);
      timeRes.setGameTime(wakeUpInstant, world, store);

      transitionPlayersToAwake(world, store, wakeUpInstant);
   }

   private Instant calculateWakeUpInstant(WorldTimeResource timeRes) {
      double wakeUpTime = configManager.getConfig().getWakeUpTime();
      int hours = (int) wakeUpTime;
      int minutes = (int) ((wakeUpTime % 1.0) * 60.0);

      return LocalDateTime.ofInstant(timeRes.getGameTime(), ZoneOffset.UTC)
            .toLocalDate()
            .plusDays(1L)
            .atTime(hours, minutes)
            .toInstant(ZoneOffset.UTC);
   }

   private void transitionPlayersToAwake(World world, Store<EntityStore> store, Instant wakeUpInstant) {
      for (PlayerRef p : world.getPlayerRefs()) {
         Ref<EntityStore> ref = p.getReference();
         if (ref == null) continue;

         PlayerSomnolence som = (PlayerSomnolence) store.getComponent(ref, PlayerSomnolence.getComponentType());
         if (som != null && isPlayerSleeping(som)) {
            store.putComponent(ref, PlayerSomnolence.getComponentType(), 
                  new PlayerSomnolence(new PlayerSleep.MorningWakeUp(wakeUpInstant)));
         }
      }
   }

   private boolean isPlayerSleeping(PlayerSomnolence som) {
      return som.getSleepState() instanceof PlayerSleep.NoddingOff || 
             som.getSleepState() instanceof PlayerSleep.Slumber;
   }
}