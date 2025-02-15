package com.github.razorplay01.ismah;

import com.github.razorplay01.ismah.util.AzureArmorRenderHandler;
import com.github.razorplay01.ismah.util.AzureLibArmorRenderHandler;
import com.github.razorplay01.ismah.util.GeckoArmorRenderHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ISMAH.MOD_ID)
public class ISMAHNeoForge {
    public ISMAHNeoForge(IEventBus eventBus) {
        if (isModLoaded("playeranimator")) {
            if (isModLoaded("geckolib")) {
                NeoForge.EVENT_BUS.register(GeckoArmorRenderHandler.class);
                ISMAH.LOGGER.info("GeckoLib and PlayerAnimator detected. Registering GeckoArmorRenderHandler.");
            }
            if (isModLoaded("azurelibarmor")) {
                NeoForge.EVENT_BUS.register(AzureArmorRenderHandler.class);
                ISMAH.LOGGER.info("AzureLibArmor and PlayerAnimator detected. Registering AzureArmorRenderHandler.");
            }
            if (isModLoaded("azurelib")) {
                NeoForge.EVENT_BUS.register(AzureLibArmorRenderHandler.class);
                ISMAH.LOGGER.info("AzureLib and PlayerAnimator detected. Registering AzureLibArmorRenderHandler.");
            }
        }
        ISMAH.init();
    }

    private boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}