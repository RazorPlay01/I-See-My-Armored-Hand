package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.api.ArmorRendererRegistry;
import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "renderRightHand", at = @At("TAIL"))
    private void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, CallbackInfo ci) {
        renderArmor(poseStack, buffer, combinedLight, player, HumanoidArm.RIGHT);
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    private void renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, CallbackInfo ci) {
        renderArmor(poseStack, buffer, combinedLight, player, HumanoidArm.LEFT);
    }

    @Unique
    private void renderArmor(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player, HumanoidArm arm) {
        if (player.isInvisible()) return;
        ItemStack armor = player.getInventory().getArmor(2);
        if (armor.isEmpty() || armor.is(Items.AIR) || !(armor.getItem() instanceof ArmorItem)) return;

        PlayerModel playerModel = (PlayerModel) ((LivingEntityRendererAccessor) this).getModel();
        playerModel.attackTime = 0.0F;
        playerModel.crouching = false;
        playerModel.swimAmount = 0.0F;
        playerModel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        CustomArmorRenderer customRenderer = ArmorRendererRegistry.getRenderer(armor);

        if (customRenderer != null) {
            customRenderer.render(poseStack, buffer, combinedLight, armor, arm, playerModel);
        } else {
            ArmorItem armorItem = (ArmorItem) armor.getItem();

            boolean isSlim = ((PlayerModelAccessor) playerModel).isSlim();
            HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(
                    HumanoidModel.createMesh(new CubeDeformation(isSlim ? 0.75f : 1.0f), 0.0f).getRoot().bake(64, 32)
            );
            playerModel.copyPropertiesTo(armorModel);
            ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
            ModelPart playerArm = arm == HumanoidArm.LEFT ? ((PlayerModel<?>) playerModel).leftArm : ((PlayerModel<?>) playerModel).rightArm;
            armorArm.copyFrom(playerArm);
            armorArm.xRot = 0.0F;

            renderEquipmentLayers(poseStack, buffer, combinedLight, armor, armorItem, armorArm);
            renderArmorTrim(poseStack, buffer, combinedLight, armor, armorItem, armorArm);
            renderGlintIfNeeded(poseStack, buffer, combinedLight, armor, armorArm);
        }
    }

    @Unique
    private void renderEquipmentLayers(PoseStack matrices, MultiBufferSource vertexConsumers,
                                       int light, ItemStack armor, ArmorItem armorItem, ModelPart armorArm) {
        ArmorMaterial armorMaterial = armorItem.getMaterial().value();
        if (!armorMaterial.layers().isEmpty()) {
            int dyeColor = armor.is(ItemTags.DYEABLE) ? DyedItemColor.getOrDefault(armor, 0) : 0;
            boolean hasFoil = armor.hasFoil();

            for (ArmorMaterial.Layer layer : armorMaterial.layers()) {
                int color = getColorForLayer(layer, dyeColor, armor);
                if (color != 0) {
                    ResourceLocation texture = layer.texture(false);
                    VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(texture), hasFoil);
                    armorArm.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
                    hasFoil = false;
                }
            }
        }
    }

    @Unique
    private int getColorForLayer(ArmorMaterial.Layer layer, int dyeColor, ItemStack armor) {
        var dyeable = layer.dyeable();
        if (dyeable) {
            int defaultColor = FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(armor, -6265536));
            return dyeColor != 0 ? dyeColor : defaultColor;
        }
        return -1;
    }

    @Unique
    private void renderArmorTrim(PoseStack matrices, MultiBufferSource vertexConsumers,
                                 int light, ItemStack armor, ArmorItem armorItem, ModelPart armorArm) {
        ArmorTrim trim = armor.get(DataComponents.TRIM);
        if (trim != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET)
                    .apply(trim.outerTexture(armorItem.getMaterial()));
            VertexConsumer trimVertexConsumer = sprite.wrap(
                    vertexConsumers.getBuffer(Sheets.armorTrimsSheet((trim.pattern().value()).decal())));
            armorArm.render(matrices, trimVertexConsumer, light, OverlayTexture.NO_OVERLAY);
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
