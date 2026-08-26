package com.github.razorplay01.ismah.mixin.accesor;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccesor {
	@Invoker("addLayer")
	boolean ismah$addLayer(RenderLayer layer);
}
