package com.github.razorplay01.ismah.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import com.github.razorplay01.ismah.ISMAH;

public final class ISMAHFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        ISMAH.init();
    }

    @Override
    public void onInitializeClient() {
        // []
    }
}
