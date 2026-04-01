package com.example.earlysleep.core;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Service responsible for applying recovery statistics and beneficial effects 
 * to players upon completing a sleep cycle.
 */
public class PlayerRecoveryService {

   private static final String ASSET_HEALTH_REGEN = "Food_Health_Regen_Small";
   private static final String ASSET_STAMINA_REGEN = "Potion_Stamina_Regen";

   /**
    * Applies health/stamina max recovery and localized regen buffs.
    *
    * @param ref   Reference to the player entity
    * @param store The entity store containing player components
    */
   public void applyWakeUpBuffs(Ref<EntityStore> ref, Store<EntityStore> store) {
      restoreMaximumStatistics(ref, store);
      applyRegenerationEffects(ref, store);
   }

   private void restoreMaximumStatistics(Ref<EntityStore> ref, Store<EntityStore> store) {
      EntityStatMap stats = (EntityStatMap) store.getComponent(ref, EntityStatMap.getComponentType());
      if (stats != null) {
         stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
         stats.maximizeStatValue(DefaultEntityStatTypes.getStamina());
      }
   }

   private void applyRegenerationEffects(Ref<EntityStore> ref, Store<EntityStore> store) {
      EffectControllerComponent effectController = (EffectControllerComponent) store.getComponent(
            ref, EffectControllerComponent.getComponentType()
      );
      
      if (effectController == null) {
         return;
      }

      EntityEffect healthRegen = (EntityEffect) EntityEffect.getAssetMap().getAsset(ASSET_HEALTH_REGEN);
      if (healthRegen != null) {
         effectController.addEffect(ref, healthRegen, 15.0F, OverlapBehavior.OVERWRITE, store);
      }

      EntityEffect staminaRegen = (EntityEffect) EntityEffect.getAssetMap().getAsset(ASSET_STAMINA_REGEN);
      if (staminaRegen != null) {
         effectController.addEffect(ref, staminaRegen, 5.0F, OverlapBehavior.OVERWRITE, store);
      }
   }
}