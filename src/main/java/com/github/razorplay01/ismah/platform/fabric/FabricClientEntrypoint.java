package com.github.razorplay01.ismah.platform.fabric;

//? fabric {

import com.github.razorplay01.ismah.ModTemplate;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ModTemplate.onInitializeClient();
	}

}
//?}
