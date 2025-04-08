package com.github.razorplay01.ismah;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ISMAH.MOD_ID)
public class ISMAHNeoForge {
    public ISMAHNeoForge(IEventBus eventBus) {
        ISMAH.init();
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}