package com.github.razorplay01.ismah;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GeckoLibCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof GeoItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<HumanoidRenderState> playerModel) {
        GeoRenderProvider geoRenderProvider = GeoRenderProvider.of(stack);
        /*if (geoRenderProvider != null) {
            GeoItem geoItem = (GeoItem) stack.getItem();

            GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geoRenderProvider.getGeoArmorRenderer(null, stack, EquipmentSlot.CHEST, EquipmentClientInfo.LayerType.HUMANOID, playerModel);
            geoArmorRenderer.preRender(new HumanoidRenderState(), poseStack, geoArmorRenderer.getGeoModel().getBakedModel(), bufferSource);
            GeoModel<GeoAnimatable> geoModel = geoArmorRenderer.getGeoModel();
            ResourceLocation texture = geoModel.getTextureResource(geoItem, geoArmorRenderer);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, geoModel.getRenderType(geoItem, texture), stack.hasFoil());

            playerModel.copyPropertiesTo(geoArmorRenderer);
            geoArmorRenderer.renderToBuffer(poseStack, vertexConsumer, light, arm == HumanoidArm.LEFT ? 1 : 0);
        }*/
    }
}