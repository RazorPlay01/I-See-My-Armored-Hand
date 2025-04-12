package com.github.razorplay01.ismah;

import com.github.razorplay01.ismah.api.ArmorRendererRegistry;
import com.github.razorplay01.ismah.compat.*;
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
        if (isModLoaded("armoroftheages")) {
            ArmorRendererRegistry.register(new ArmorOfTheAgesCompat());
            ISMAH.LOGGER.info("Armor of the Ages detected. Registering ArmorOfTheAgesCompat.");
        }
        if (isModLoaded("rootsclassic")) {
            ArmorRendererRegistry.register(new RootsClassicCompat());
            ISMAH.LOGGER.info("RootsClassic detected. Registering RootsClassicCompat.");
        }
        if (isModLoaded("cataclysm")) {
            ArmorRendererRegistry.register(new CataclysmCompat());
            ISMAH.LOGGER.info("Cataclysm detected. Registering CataclysmCompat.");
        }
        if (isModLoaded("alkhars")) {
            ArmorRendererRegistry.register(new AlchemistsArsenalCompat());
            ISMAH.LOGGER.info("Alchemist's Arsenal detected. Registering AlchemistsArsenalCompat.");
        }
        if (isModLoaded("geckolib")) {
            ArmorRendererRegistry.register(new GeckoLibCompat());
            ISMAH.LOGGER.info("GeckoLib detected. Registering AzureArmorLibCompat.");
        }
        if (isModLoaded("azurelibarmor")) {
            ArmorRendererRegistry.register(new AzureArmorLibCompat());
            ISMAH.LOGGER.info("AzureLibArmor detected. Registering AzureArmorLibCompat.");
        }
        if (isModLoaded("azurelib")) {
            ArmorRendererRegistry.register(new AzureLibCompat());
            ISMAH.LOGGER.info("AzureLib detected. Registering AzureArmorLibCompat.");
        }
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