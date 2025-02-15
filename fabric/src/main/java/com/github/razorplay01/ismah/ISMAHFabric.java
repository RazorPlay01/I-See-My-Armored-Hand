package com.github.razorplay01.ismah;

import com.github.razorplay01.ismah.util.AzureArmorRenderHandler;
import com.github.razorplay01.ismah.util.AzureLibArmorRenderHandler;
import com.github.razorplay01.ismah.util.GeckoArmorRenderHandler;
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
        if (isModLoaded("playeranimator")) {
            if (isModLoaded("geckolib")) {
                GeckoArmorRenderHandler.register();
                ISMAH.LOGGER.info("GeckoLib and PlayerAnimator detected. Registering GeckoArmorRenderHandler.");
            }
            if (isModLoaded("azurelibarmor")) {
                AzureArmorRenderHandler.register();
                ISMAH.LOGGER.info("AzureLibArmor and PlayerAnimator detected. Registering AzureArmorRenderHandler.");
            }
            if (isModLoaded("azurelib")) {
                AzureLibArmorRenderHandler.register();
                ISMAH.LOGGER.info("AzureLib and PlayerAnimator detected. Registering AzureLibArmorRenderHandler.");
            }
        }
        ISMAH.init();
    }

    private boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}