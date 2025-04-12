package com.github.razorplay01.ismah.compat;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GeckolibCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof GeoItem && stack.getItem() instanceof ArmorItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack stack, HumanoidArm arm, HumanoidModel playerModel) {
        if (!(stack.getItem() instanceof GeoItem)) return;

        LocalPlayer player = Minecraft.getInstance().player;

        HumanoidModel<?> geoArmorRenderer = GeoRenderProvider.of(stack).getGeoArmorRenderer(player, stack, EquipmentSlot.CHEST, playerModel);
        if (geoArmorRenderer != null) {
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(ResourceLocation.withDefaultNamespace("")), stack.hasFoil());
            playerModel.copyPropertiesTo(geoArmorRenderer);
            boolean isRightArm = arm == HumanoidArm.RIGHT;
            configureBoneVisibility((GeoArmorRenderer<?>) geoArmorRenderer, isRightArm);

            geoArmorRenderer.renderToBuffer(poseStack, vertexConsumer, light, arm == HumanoidArm.LEFT ? 1 : 0);
        }

        resetBoneVisibility((GeoArmorRenderer<?>) geoArmorRenderer);
    }

    private void configureBoneVisibility(GeoArmorRenderer<?> renderer, boolean isRightArm) {
        renderer.getGeoModel().getBone("rightArm").ifPresent(bone -> bone.setHidden(!isRightArm));
        renderer.getGeoModel().getBone("leftArm").ifPresent(bone -> bone.setHidden(isRightArm));
        renderer.getGeoModel().getBone("head").ifPresent(bone -> bone.setHidden(true));
        renderer.getGeoModel().getBone("body").ifPresent(bone -> bone.setHidden(true));
        renderer.getGeoModel().getBone("rightLeg").ifPresent(bone -> bone.setHidden(true));
        renderer.getGeoModel().getBone("leftLeg").ifPresent(bone -> bone.setHidden(true));
    }

    private void resetBoneVisibility(GeoArmorRenderer<?> renderer) {
        renderer.getGeoModel().getBone("rightArm").ifPresent(bone -> bone.setHidden(false));
        renderer.getGeoModel().getBone("leftArm").ifPresent(bone -> bone.setHidden(false));
        renderer.getGeoModel().getBone("head").ifPresent(bone -> bone.setHidden(false));
        renderer.getGeoModel().getBone("body").ifPresent(bone -> bone.setHidden(false));
        renderer.getGeoModel().getBone("rightLeg").ifPresent(bone -> bone.setHidden(false));
        renderer.getGeoModel().getBone("leftLeg").ifPresent(bone -> bone.setHidden(false));
    }
}
