package com.github.razorplay01.ismah.mixin.accesor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >= 1.21.9 {
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
//?}
//? if >= 1.21.2 && < 1.21.9 {
/*import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
*///?}
//? if < 1.21.2 {
/*import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
*///?}

/**
 * Expone el estado interno de {@link HumanoidArmorLayer} para que el renderizado
 * de primera persona pueda usar los modelos de armadura y el resolutor de
 * texturas de equipo sin invocar {@code render}/{@code submit} (que es donde
 * otros mods inyectan su propio renderizado y provocan que se pinte el peto
 * completo en el brazo).
 */
@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor {

	//? if >= 1.21.9 {
	@Accessor("modelSet")
	ArmorModelSet<? extends HumanoidModel<?>> ismah$modelSet();

	@Accessor("babyModelSet")
	ArmorModelSet<? extends HumanoidModel<?>> ismah$babyModelSet();

	@Accessor("equipmentRenderer")
	EquipmentLayerRenderer ismah$equipmentRenderer();
	//?}
	//? if >= 1.21.2 && < 1.21.9 {
	/*@Accessor("outerModel")
	HumanoidModel<?> ismah$outerModel();

	@Accessor("outerModelBaby")
	HumanoidModel<?> ismah$outerModelBaby();

	@Accessor("equipmentRenderer")
	EquipmentLayerRenderer ismah$equipmentRenderer();
	*///?}
	//? if < 1.21.2 {
	/*@Accessor("outerModel")
	HumanoidModel<?> ismah$outerModel();
	*///?}
}
