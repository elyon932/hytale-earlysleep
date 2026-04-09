package com.example.earlysleep.service;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class HealthRecoveryService {
    public void recover(Ref<EntityStore> ref, Store<EntityStore> store) {
        EntityStatMap stats = (EntityStatMap)store.getComponent(ref, EntityStatMap.getComponentType());
        if (stats != null) {
            stats.maximizeStatValue(DefaultEntityStatTypes.getHealth());
            stats.maximizeStatValue(DefaultEntityStatTypes.getStamina());
        }
    }
}