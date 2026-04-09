package com.example.earlysleep.service;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class BuffEffectService {
    public void applyBuffs(Ref<EntityStore> ref, Store<EntityStore> store) {
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
}