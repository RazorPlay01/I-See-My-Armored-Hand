package com.github.razorplay01.ismah.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.lang.reflect.Method;

import static com.github.razorplay01.ismah.ISMAH.LOGGER;

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

        Item item = chestplate.getItem();
        if (item instanceof GeoItem && item instanceof Item) {
            renderGeoArmor(matrices, vertexConsumers, light, (Item & GeoItem) item, chestplate, arm);
        } else {
            renderVanillaArmor(matrices, vertexConsumers, light, chestplate, arm);
        }
    }

    @Unique
    private <T extends Item & GeoItem> void renderGeoArmor(PoseStack matrices, MultiBufferSource vertexConsumers, int light, T geoArmorItem, ItemStack chestplate, HumanoidArm arm) {
        Object renderProvider = geoArmorItem.getRenderProvider().get();
        GeoArmorRenderer<T> renderer = null;
        try {
            Method getHumanoidArmorModel = renderProvider.getClass().getDeclaredMethod(
                    "getHumanoidArmorModel",
                    LivingEntity.class,
                    ItemStack.class,
                    EquipmentSlot.class,
                    HumanoidModel.class
            );

            getHumanoidArmorModel.setAccessible(true);
            HumanoidModel<?> model = ((PlayerRenderer) MINECRAFT_CLIENT.getEntityRenderDispatcher().getRenderer(MINECRAFT_CLIENT.player)).getModel();
            Object result = getHumanoidArmorModel.invoke(
                    renderProvider,
                    MINECRAFT_CLIENT.player,
                    chestplate,
                    EquipmentSlot.CHEST,
                    model
            );

            if (result instanceof GeoArmorRenderer) {
                @SuppressWarnings("unchecked")
                GeoArmorRenderer<T> castedRenderer = (GeoArmorRenderer<T>) result;
                renderer = castedRenderer;
            }
        } catch (Exception e) {
            LOGGER.error("Error getting the renderer: ", e);
        }

        if (renderer == null) {
            LOGGER.error("Could not get renderer for GeoArmorItem");
            return;
        }

        matrices.pushPose();
        matrices.translate(0, 1.5f, 0);
        matrices.mulPose(Axis.ZP.rotationDegrees(180));
        matrices.mulPose(Axis.YP.rotationDegrees(0));
        matrices.mulPose(Axis.XP.rotationDegrees(0));
        renderer.prepForRender(MINECRAFT_CLIENT.player, chestplate, EquipmentSlot.CHEST, ((PlayerRenderer) MINECRAFT_CLIENT.getEntityRenderDispatcher().getRenderer(MINECRAFT_CLIENT.player)).getModel());

        GeoBone armBone = arm == HumanoidArm.LEFT ? renderer.getLeftArmBone() : renderer.getRightArmBone();

        if (armBone != null) {
            ResourceLocation textureLocation = renderer.getTextureLocation(geoArmorItem);
            BakedGeoModel model = renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(geoArmorItem, renderer));
            RenderType renderType = renderer.getRenderType(geoArmorItem, textureLocation, vertexConsumers, 0.0F);

            VertexConsumer buffer = vertexConsumers.getBuffer(renderType);

            float r = 1.0F;
            float g = 1.0F;
            float b = 1.0F;
            if (chestplate.getItem() instanceof DyeableArmorItem dyeableArmorItem) {
                int color = dyeableArmorItem.getColor(chestplate);
                r = (color >> 16 & 255) / 255.0F;
                g = (color >> 8 & 255) / 255.0F;
                b = (color & 255) / 255.0F;
            }

            renderBoneAndChildren(matrices, armBone, geoArmorItem, model, renderType, vertexConsumers, buffer, light, r, g, b);
        }
        matrices.popPose();
    }

    @Unique
    private void renderBoneAndChildren(PoseStack matrices, GeoBone bone, GeoItem geoArmorItem, BakedGeoModel model, RenderType renderType, MultiBufferSource vertexConsumers, VertexConsumer buffer, int light, float r, float g, float b) {
        if (bone.isHidden()) return;
        for (GeoCube cube : bone.getCubes()) {
            matrices.pushPose();
            for (GeoQuad quad : cube.quads()) {
                Vector3f normalVector = quad.normal();
                PoseStack.Pose lastPose = matrices.last();
                Matrix3f normalMatrix = lastPose.normal(); // Obtén la matriz normal
                //normalMatrix.transform(normalVector);         // Transforma la normal
                normalVector.normalize();                      // Normaliza la normal

                for (GeoVertex vertex : quad.vertices()) {
                    Vector4f position = new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0F);
                    lastPose.pose().transform(position);

                    buffer.vertex(position.x(), position.y(), position.z(), r, g, b, 1.0F, vertex.texU(), vertex.texV(), light, OverlayTexture.NO_OVERLAY, normalVector.x(), normalVector.y(), normalVector.z());
                }
            }
            matrices.popPose();
        }

        for (GeoBone childBone : bone.getChildBones()) {
            if (!childBone.isHidden() && !bone.isHidingChildren()) {
                renderBoneAndChildren(matrices, childBone, geoArmorItem, model, renderType, vertexConsumers, buffer, light, r, g, b);
            }
        }
    }

    @Unique
    private void renderVanillaArmor(PoseStack matrices, MultiBufferSource vertexConsumers, int light, ItemStack chestplate, HumanoidArm arm) {
        ArmorItem armorItem = (ArmorItem) chestplate.getItem();
        ModelPart armorArm = setupArmorModel(MINECRAFT_CLIENT.player, arm); // Use your existing setup method

        if (armorItem instanceof DyeableArmorItem dyeableArmorItem) {
            int color = dyeableArmorItem.getColor(chestplate);
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            renderArmorLayer(matrices, vertexConsumers, light, armorItem, armorArm, r, g, b, null);
            renderArmorLayer(matrices, vertexConsumers, light, armorItem, armorArm, 1.0F, 1.0F, 1.0F, "overlay");
        } else {
            renderArmorLayer(matrices, vertexConsumers, light, armorItem, armorArm, 1.0F, 1.0F, 1.0F, null);
        }

        ArmorTrim.getTrim(MINECRAFT_CLIENT.level.registryAccess(), chestplate).ifPresent(armorTrim -> renderTrim(matrices, vertexConsumers, light, armorTrim, armorItem, armorArm));
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
    private void renderArmorLayer(PoseStack matrices, MultiBufferSource vertexConsumers, int light,
                                  ArmorItem armorItem, ModelPart armorPart, float r, float g, float b, @Nullable String overlay) {
        ResourceLocation armorTexture = getArmorTexture(armorItem, overlay);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.armorCutoutNoCull(armorTexture));
        armorPart.render(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    @Unique
    private ResourceLocation getArmorTexture(ArmorItem armorItem, @Nullable String overlay) {
        String path = "textures/models/armor/" + armorItem.getMaterial().getName() + "_layer_1" +
                (overlay == null ? "" : "_" + overlay) + ".png";
        return new ResourceLocation(path);
    }

    @Unique
    private void renderTrim(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ArmorTrim armorTrim, ArmorItem armorItem, ModelPart humanoidModel) {
        TextureAtlasSprite textureAtlasSprite = MINECRAFT_CLIENT.getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET)
                .apply(armorTrim.outerTexture(armorItem.getMaterial()));
        VertexConsumer vertexConsumer = textureAtlasSprite.wrap(multiBufferSource.getBuffer(Sheets.armorTrimsSheet()));
        humanoidModel.render(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Unique
    private void renderGlintIfNeeded(PoseStack matrices, MultiBufferSource vertexConsumers, int light, ItemStack chestplate, ModelPart armorArm) {
        if (chestplate.hasFoil()) {
            VertexConsumer glintConsumer = vertexConsumers.getBuffer(RenderType.armorEntityGlint());
            armorArm.render(matrices, glintConsumer, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
