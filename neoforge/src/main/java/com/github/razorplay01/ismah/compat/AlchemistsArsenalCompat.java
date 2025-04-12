package com.github.razorplay01.ismah.compat;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import github.nitespring.alchemistarsenal.client.render.equipment.TurtleMasterArmourModel;
import github.nitespring.alchemistarsenal.common.item.equipment.TurtleMasterArmourItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AlchemistsArsenalCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof TurtleMasterArmourItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<@NotNull LivingEntity> playerModel) {
        renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                new TurtleMasterArmourModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(TurtleMasterArmourModel.LAYER_LOCATION)),
                TurtleMasterArmourItem.TEXTURE_LOCATION_OUTER_LAYER);
    }
}
