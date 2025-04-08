package com.github.razorplay01.ismah.api;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

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

    /**
     * Renders the armor on the player's arm in first-person view.
     * Implementations have full control over the rendering process, including textures, colors, and effects.
     *
     * @param poseStack The transformation matrix stack for rendering.
     * @param vertexConsumers The buffer source for rendering vertices.
     * @param armorArm The ModelPart representing the arm where the armor is rendered (left or right).
     * @param light The light level for rendering.
     * @param stack The ItemStack of the equipped armor.
     * @param arm The arm being rendered (either {@link HumanoidArm#LEFT} or {@link HumanoidArm#RIGHT}).
     */
    void render(PoseStack poseStack, MultiBufferSource vertexConsumers, ModelPart armorArm, int light, ItemStack stack, HumanoidArm arm);
}