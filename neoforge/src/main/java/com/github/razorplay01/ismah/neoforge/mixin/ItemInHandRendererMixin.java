package com.github.razorplay01.ismah.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Unique
    private static final Minecraft MINECRAFT_CLIENT = Minecraft.getInstance();

    @Inject(method = "renderPlayerArm", at = @At("TAIL"))
    private void renderArmorLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm, CallbackInfo ci) {
        if (MINECRAFT_CLIENT.player == null || MINECRAFT_CLIENT.player.isInvisible()) return;
        renderArmor(poseStack, multiBufferSource, MINECRAFT_CLIENT.player, humanoidArm, i);
    }
    @Unique
    private void renderArmor(PoseStack matrices, MultiBufferSource vertexConsumers,
                             LocalPlayer player, HumanoidArm arm, int light) {
        ItemStack chestplate = player.getInventory().getArmor(2);
        if (!isValidChestplate(chestplate)) return;

        ArmorItem armorItem = (ArmorItem) chestplate.getItem();
        ModelPart armorArm = setupArmorModel(player, arm);

        renderArmorLayers(matrices, vertexConsumers, light, chestplate, armorItem, armorArm);
        renderArmorTrim(matrices, vertexConsumers, light, chestplate, armorItem, armorArm);
        renderGlintIfNeeded(matrices, vertexConsumers, light, chestplate, armorArm);
    }
    @Unique
    private boolean isValidChestplate(ItemStack item) {
        return item.getItem() instanceof ArmorItem armorItem &&
                armorItem.getEquipmentSlot() == EquipmentSlot.CHEST;
    }
    @Unique
    private ModelPart setupArmorModel(LocalPlayer player, HumanoidArm arm) {
        PlayerRenderer playerRenderer = (PlayerRenderer) MINECRAFT_CLIENT.getEntityRenderDispatcher()
                .getRenderer(player);
        PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();

        HumanoidModel<LivingEntity> armorModel = new HumanoidModel<>(
                HumanoidModel.createMesh(new CubeDeformation(1.0f), 0.0f).getRoot().bake(64, 32)
        );

        ModelPart armorArm = arm == HumanoidArm.LEFT ? armorModel.leftArm : armorModel.rightArm;
        ModelPart playerArm = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
        armorArm.copyFrom(playerArm);

        return armorArm;
    }
    @Unique
    private void renderArmorLayers(PoseStack matrices, MultiBufferSource vertexConsumers,
                                   int light, ItemStack chestplate, ArmorItem armorItem, ModelPart armorArm) {
        ArmorMaterial armorMaterial = armorItem.getMaterial().value();
        int color = getArmorColor(chestplate);

        for (ArmorMaterial.Layer layer : armorMaterial.layers()) {
            ResourceLocation texture = layer.texture(false);
            if (!textureExists(texture)) {
                continue;
            }

            int layerColor = layer.dyeable() ? color : -1;
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(texture));
            armorArm.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, layerColor);
        }
    }

    @Unique
    private int getArmorColor(ItemStack chestplate) {
        return chestplate.is(ItemTags.DYEABLE)
                ? FastColor.ABGR32.opaque(DyedItemColor.getOrDefault(chestplate, -6265536))
                : -1;
    }

    @Unique
    private void renderArmorTrim(PoseStack matrices, MultiBufferSource vertexConsumers,
                                 int light, ItemStack chestplate, ArmorItem armorItem, ModelPart armorArm) {
        ArmorTrim trim = chestplate.get(DataComponents.TRIM);
        if (trim == null) return;

        TextureAtlasSprite sprite = MINECRAFT_CLIENT.getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET)
                .apply(trim.outerTexture(armorItem.getMaterial()));
        VertexConsumer trimVertexConsumer = sprite.wrap(
                vertexConsumers.getBuffer(Sheets.armorTrimsSheet((trim.pattern().value()).decal())));
        armorArm.render(matrices, trimVertexConsumer, light, OverlayTexture.NO_OVERLAY);
    }

    @Unique
    private void renderGlintIfNeeded(PoseStack matrices, MultiBufferSource vertexConsumers,
                                     int light, ItemStack chestplate, ModelPart armorArm) {
        if (chestplate.hasFoil()) {
            VertexConsumer glintConsumer = vertexConsumers.getBuffer(RenderType.armorEntityGlint());
            armorArm.render(matrices, glintConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    @Unique
    private boolean textureExists(ResourceLocation texture) {
        return MINECRAFT_CLIENT.getResourceManager().getResource(texture).isPresent();
    }
}
