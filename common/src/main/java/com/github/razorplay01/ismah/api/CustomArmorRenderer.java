package com.github.razorplay01.ismah.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for custom armor renderers in first-person view.
 * Mod developers can implement this interface to define how their armor items should be rendered
 * on the player's arm in first-person perspective when registered with {@link ArmorRendererRegistry}.
 */
public interface CustomArmorRenderer {
    /**
     * Determines whether this renderer can handle the given armor ItemStack.
     * This method is called to check if the renderer should be used for a specific item.
     *
     * @param stack The ItemStack of the equipped armor.
     * @return {@code true} if this renderer should handle the ItemStack, {@code false} otherwise.
     */
    boolean canRender(ItemStack stack);

    void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<@NotNull LivingEntity> playerModel);

    default void renderArmor(PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<@NotNull LivingEntity> playerModel, HumanoidModel<@NotNull LivingEntity> armorModel, ResourceLocation texture) {
        ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
        ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
        armorArm.copyFrom(playerArm);

        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(texture), stack.hasFoil());
        armorArm.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
    }
}