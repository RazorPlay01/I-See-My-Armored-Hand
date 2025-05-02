package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.api.ArmorRendererRegistry;
import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentModel;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "renderRightHand", at = @At("TAIL"))
    private void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation p_363694_, boolean isSleeveVisible, CallbackInfo ci) {
        renderArmor(poseStack, buffer, combinedLight, HumanoidArm.RIGHT, isSleeveVisible);
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    private void renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, ResourceLocation p_361745_, boolean isSleeveVisible, CallbackInfo ci) {
        renderArmor(poseStack, buffer, combinedLight, HumanoidArm.LEFT, isSleeveVisible);
    }

    @Unique
    private void renderArmor(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, HumanoidArm arm, boolean isSleeveVisible) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player.isInvisible()) return;
        ItemStack armor = player.getInventory().getArmor(2);
        if (armor.isEmpty() || armor.is(Items.AIR) || !(armor.getItem() instanceof ArmorItem)) return;
        Equippable equippable = armor.get(DataComponents.EQUIPPABLE);

        EntityModel<?> entityModel = ((LivingEntityRendererAccessor) this).getModel();
        PlayerModel playerModel = (PlayerModel) entityModel;

        ModelPart armModel = arm == HumanoidArm.RIGHT ? playerModel.rightArm : playerModel.leftArm;
        armModel.resetPose();
        armModel.visible = true;
        playerModel.leftSleeve.visible = isSleeveVisible;
        playerModel.rightSleeve.visible = isSleeveVisible;
        playerModel.leftArm.zRot = -0.1F;
        playerModel.rightArm.zRot = 0.1F;

        CustomArmorRenderer customRenderer = ArmorRendererRegistry.getRenderer(armor);

        if (customRenderer != null) {
            customRenderer.render(poseStack, buffer, combinedLight, armor, arm, (HumanoidModel<HumanoidRenderState>) entityModel);
        } else {
            boolean isSlim = ((PlayerModelAccessor) playerModel).isSlim();
            HumanoidModel<HumanoidRenderState> armorModel = new HumanoidModel<>(
                    HumanoidModel.createMesh(new CubeDeformation(isSlim ? 0.75f : 1.0f), 0.0f).getRoot().bake(64, 32)
            );
            playerModel.copyPropertiesTo(playerModel);

            ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
            ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
            armorArm.copyFrom(playerArm);
            armorArm.xRot = 0.0F;

            renderEquipmentLayers(poseStack, buffer, armor, equippable, armorArm, combinedLight);
            renderArmorTrim(poseStack, buffer, armor, equippable, armorArm, combinedLight);
            renderGlintIfNeeded(poseStack, buffer, combinedLight, armor, armorArm);
        }
    }

    @Unique
    private void renderEquipmentLayers(PoseStack poseStack, MultiBufferSource vertexConsumers, ItemStack chestplate, Equippable equippable, ModelPart armorArm, int light) {
        ResourceLocation modelId = equippable.model().orElseThrow();
        List<EquipmentModel.Layer> layers = Minecraft.getInstance().getEquipmentModels().get(modelId).getLayers(EquipmentModel.LayerType.HUMANOID);

        if (!layers.isEmpty()) {
            int dyeColor = chestplate.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(chestplate, 0) : 0;
            boolean hasFoil = chestplate.hasFoil();

            for (EquipmentModel.Layer layer : layers) {
                int color = getColorForLayer(layer, dyeColor);
                if (color != 0) {
                    ResourceLocation texture = layer.getTextureLocation(EquipmentModel.LayerType.HUMANOID);
                    VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(texture), hasFoil);
                    armorArm.render(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
                    hasFoil = false;
                }
            }
        }
    }

    @Unique
    private int getColorForLayer(EquipmentModel.Layer layer, int dyeColor) {
        var dyeable = layer.dyeable();
        if (dyeable.isPresent()) {
            int defaultColor = dyeable.get().colorWhenUndyed().map(color -> 0xFF000000 | color).orElse(0);
            return dyeColor != 0 ? dyeColor : defaultColor;
        }
        return -1;
    }

    @Unique
    private void renderArmorTrim(PoseStack poseStack, MultiBufferSource vertexConsumers, ItemStack chestplate, Equippable equippable, ModelPart armorArm, int light) {
        ArmorTrim trim = chestplate.get(DataComponents.TRIM);
        if (trim != null) {
            ResourceLocation modelId = equippable.model().orElseThrow();
            ResourceLocation trimTextureId = trim.getTexture(EquipmentModel.LayerType.HUMANOID, modelId);
            TextureAtlasSprite trimSprite = Minecraft.getInstance().getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET).apply(trimTextureId);
            VertexConsumer trimConsumer = trimSprite.wrap(vertexConsumers.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
            armorArm.render(poseStack, trimConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    @Unique
    private void renderGlintIfNeeded(PoseStack matrices, MultiBufferSource vertexConsumers,
                                     int light, ItemStack armor, ModelPart armorArm) {
        if (armor.hasFoil()) {
            VertexConsumer glintConsumer = vertexConsumers.getBuffer(RenderType.entityGlint());
            armorArm.render(matrices, glintConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
