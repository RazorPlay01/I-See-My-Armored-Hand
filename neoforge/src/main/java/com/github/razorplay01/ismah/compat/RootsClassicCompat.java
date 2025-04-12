package com.github.razorplay01.ismah.compat;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import elucent.rootsclassic.client.ClientHandler;
import elucent.rootsclassic.client.model.SylvanArmorModel;
import elucent.rootsclassic.client.model.WildwoodArmorModel;
import elucent.rootsclassic.item.SylvanArmorItem;
import elucent.rootsclassic.item.WildwoodArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RootsClassicCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof SylvanArmorItem || stack.getItem() instanceof WildwoodArmorItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<@NotNull LivingEntity> playerModel) {
        if (stack.getItem() instanceof SylvanArmorItem) {
            HumanoidModel<LivingEntity> armorModel = new SylvanArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientHandler.SYLVAN_ARMOR), ArmorItem.Type.BODY);
            ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
            ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
            armorArm.copyFrom(playerArm);

            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(ResourceLocation.fromNamespaceAndPath("rootsclassic", "textures/models/armor/sylvan.png")), stack.hasFoil());
            armorArm.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        } else if (stack.getItem() instanceof WildwoodArmorItem) {
            HumanoidModel<LivingEntity> armorModel = new WildwoodArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientHandler.WILDWOOD_ARMOR), ArmorItem.Type.BODY);
            ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
            ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
            armorArm.copyFrom(playerArm);

            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(ResourceLocation.fromNamespaceAndPath("rootsclassic", "textures/models/armor/wildwood.png")), stack.hasFoil());
            armorArm.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
