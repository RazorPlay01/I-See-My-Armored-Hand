package com.github.razorplay01.ismah;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ISMAHFabric implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {
        // []
    }

    @Override
    public void onInitializeClient() {
        ISMAH.init();
    }

    private boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}