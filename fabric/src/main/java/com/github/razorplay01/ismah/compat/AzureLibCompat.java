package com.github.razorplay01.ismah.compat;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class AzureLibCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem &&
                AzArmorRendererRegistry.getOrNull(stack) != null;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<LivingEntity> playerModel) {
        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(stack);

        renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                (HumanoidModel<LivingEntity>) renderer.rendererPipeline().armorModel(),
                renderer.rendererPipeline().config().textureLocation(stack));

        /* AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(stack);
        renderer.prepForRender(Minecraft.getInstance().player, stack, EquipmentSlot.CHEST, playerModel);

        HumanoidModel<LivingEntity> armorModel = (HumanoidModel<LivingEntity>) renderer.rendererPipeline().armorModel();
        playerModel.copyPropertiesTo(armorModel);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(renderer.rendererPipeline().config().textureLocation(stack)));
        armorModel.renderToBuffer(poseStack, vertexConsumer, light, arm == HumanoidArm.LEFT ? 1 : 0);*/
    }
}