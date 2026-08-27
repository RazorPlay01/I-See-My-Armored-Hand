package com.github.razorplay01.ismah.mixin.accesor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if <1.21.11{
/*import net.minecraft.client.model.PlayerModel;
*///?}
//? if >=1.21.11{
import net.minecraft.client.model.player.PlayerModel;
//?}

@Mixin(PlayerModel.class)
public interface PlayerModelAccesor {
	@Accessor("slim")
	boolean ismah$getSlim();
}
