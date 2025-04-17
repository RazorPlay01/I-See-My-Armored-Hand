package com.github.razorplay01.ismah;

import com.github.razorplay01.ismah.api.ArmorRendererRegistry;
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
        if (isModLoaded("geckolib")) {
            ArmorRendererRegistry.register(new GeckoLibCompat());
            ISMAH.LOGGER.info("GeckoLib detected. Registering AzureArmorLibCompat.");
        }
        ISMAH.init();
    }

    private boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}