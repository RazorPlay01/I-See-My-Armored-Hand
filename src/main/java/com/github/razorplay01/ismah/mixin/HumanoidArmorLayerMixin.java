package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=1.21.2{
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
//?}
//? if >=1.21.9{
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}

@Mixin(HumanoidArmorLayer.class)
//? < 1.21.2 {
/*public abstract class HumanoidArmorLayerMixin<S extends net.minecraft.world.entity.LivingEntity, M extends HumanoidModel<S>, A extends HumanoidModel<S>>
		extends RenderLayer<S, M> {
*///?} >= 1.21.2 {
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>>
        extends RenderLayer<S, M> {
//?}
    protected HumanoidArmorLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

	// Solo se permite renderizar la pieza del pecho cuando la capa recibe un estado
	// marcado como "brazo en primera persona". Se comprueba de forma defensiva para
	// que otras capas de terceros que llamen a renderArmorPiece no puedan pintar el
	// peto completo sobre el brazo.
	//? < 1.21.2 && fabric || forge {
	/*@WrapWithCondition(
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V"
			)
	)
	private boolean renderChest(HumanoidArmorLayer instance, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel humanoidModel, @Local(ordinal = 0) S state) {
		*///?} < 1.21.2 && neoforge {
	/*@WrapWithCondition(
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"
			)
	)
	private boolean renderChest(HumanoidArmorLayer instance, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, HumanoidModel humanoidModel, float f, float g, float h, float j, float k, float l, @Local(ordinal = 0) S state) {*/
		//?} >= 1.21.2 && < 1.21.9 {
	/*@WrapWithCondition(
			method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V"
			)
	)
	private boolean renderChest(HumanoidArmorLayer instance, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, ItemStack itemStack, EquipmentSlot equipmentSlot, int i, HumanoidModel humanoidModel, @Local(ordinal = 0) S state) {
	*///?} >= 1.21.9 {
	@WrapWithCondition(
			method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"
			)
	)
	private boolean renderChest(HumanoidArmorLayer instance, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ItemStack itemStack, EquipmentSlot equipmentSlot, int i, HumanoidRenderState state) {
	//?}
		// Estados que no soportan el flag de primera persona se renderizan con normalidad.
		if (!(state instanceof FirstPersonArmRenderStateAccessor accessor)) {
			return true;
		}
		return accessor.ismah$getFirstPersonArm() == null || equipmentSlot == EquipmentSlot.CHEST;
	}
}
