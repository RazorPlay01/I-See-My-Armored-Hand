package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.util.accessor.LeashStateAccessor;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayer.class)
public abstract class PlayerLeashStateMixin implements LeashStateAccessor {
	@Unique
	private boolean ismah$leashState = false;

	@Override
	public boolean ismah$getLeashState() {
		return this.ismah$leashState;
	}

	@Override
	public void ismah$setLeashState(boolean state) {
		this.ismah$leashState = state;
	}
}
