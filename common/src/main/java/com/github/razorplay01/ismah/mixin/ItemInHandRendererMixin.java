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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
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

import static com.github.razorplay01.ismah.ISMAH.MINECRAFT_CLIENT;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Inject(method = "renderPlayerArm", at = @At("TAIL"))
    private void renderArmorLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, float f, float g, HumanoidArm humanoidArm, CallbackInfo ci) {
        if (MINECRAFT_CLIENT.player == null || MINECRAFT_CLIENT.player.isInvisible()) return;
        renderArmor(poseStack, multiBufferSource, MINECRAFT_CLIENT.player, humanoidArm, light);
    }

    @Unique
    private void renderArmor(PoseStack poseStack, MultiBufferSource vertexConsumers, LocalPlayer player, HumanoidArm arm, int light) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (chestplate.isEmpty()) return;

        CustomArmorRenderer customRenderer = ArmorRendererRegistry.getRenderer(chestplate);
        ModelPart armorArm = setupArmorModel(player, arm);

        if (customRenderer != null) {
            customRenderer.render(poseStack, vertexConsumers, armorArm, light, chestplate, arm);
        } else {
            Equippable equippable = chestplate.get(DataComponents.EQUIPPABLE);
            if (chestplate.getItem() instanceof ArmorItem && equippable != null && equippable.model().isPresent() && equippable.slot() == EquipmentSlot.CHEST) {
                renderEquipment(poseStack, vertexConsumers, player, arm, light, chestplate);
            }
        }
    }

    @Unique
    private void renderEquipment(PoseStack poseStack, MultiBufferSource vertexConsumers, LocalPlayer player, HumanoidArm arm, int light, ItemStack chestplate) {
        Equippable equippable = chestplate.get(DataComponents.EQUIPPABLE);
        ModelPart armorArm = setupArmorModel(player, arm);
        renderEquipmentLayers(poseStack, vertexConsumers, chestplate, equippable, armorArm, light);
        renderTrim(poseStack, vertexConsumers, chestplate, equippable, armorArm, light);
        renderGlintIfNeeded(poseStack, vertexConsumers, light, chestplate, armorArm);
    }

    @Unique
    private ModelPart setupArmorModel(LocalPlayer player, HumanoidArm arm) {
        PlayerRenderer playerRenderer = (PlayerRenderer) MINECRAFT_CLIENT.getEntityRenderDispatcher().getRenderer(player);
        PlayerModel playerModel = playerRenderer.getModel();

        boolean isSlim = ((PlayerModelAccessor) playerModel).isSlim();
        HumanoidModel<HumanoidRenderState> armorModel = new HumanoidModel<>(
                HumanoidModel.createMesh(new CubeDeformation(isSlim ? 0.75f : 1.0f), 0.0f).getRoot().bake(64, 32)
        );

        ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
        ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
        armorArm.copyFrom(playerArm);

        return armorArm;
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
    private void renderTrim(PoseStack poseStack, MultiBufferSource vertexConsumers, ItemStack chestplate, Equippable equippable, ModelPart armorArm, int light) {
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
    private void renderGlintIfNeeded(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack chestplate, ModelPart armorArm) {
        if (chestplate.hasFoil()) {
            VertexConsumer glintConsumer = vertexConsumers.getBuffer(RenderType.entityGlint());
            armorArm.render(poseStack, glintConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }
}