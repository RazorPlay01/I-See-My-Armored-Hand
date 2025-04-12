package com.github.razorplay01.ismah.compat;

import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class GeckoLibCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof GeoItem && stack.getItem() instanceof ArmorItem;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<LivingEntity> playerModel) {
        GeoRenderProvider geoRenderProvider = GeoRenderProvider.of(stack);
        GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geoRenderProvider.getGeoArmorRenderer(Minecraft.getInstance().player, stack, EquipmentSlot.CHEST, playerModel);

        renderArmor(poseStack, bufferSource, light, stack, arm, playerModel,
                (HumanoidModel<LivingEntity>) geoArmorRenderer,
                geoArmorRenderer.getGeoModel().getTextureResource((GeoItem) stack.getItem(), geoArmorRenderer));
        /*if (geoRenderProvider != null) {
            GeoItem geoItem = (GeoItem) stack.getItem();
            GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geoRenderProvider.getGeoArmorRenderer(Minecraft.getInstance().player, stack, EquipmentSlot.CHEST, playerModel);
            GeoModel<GeoAnimatable> geoModel = geoArmorRenderer.getGeoModel();
            ResourceLocation texture = geoModel.getTextureResource(geoItem, geoArmorRenderer);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, geoModel.getRenderType(geoItem, texture), stack.hasFoil());

            playerModel.copyPropertiesTo(geoArmorRenderer);
            geoArmorRenderer.renderToBuffer(poseStack, vertexConsumer, light, arm == HumanoidArm.LEFT ? 1 : 0);
        }*/
    }
}
