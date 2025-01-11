package com.github.razorplay01.ismah.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    @Shadow
    @Final
    protected List<RenderLayer<T, M>> layers;

    @Shadow
    protected abstract float getBob(T livingEntity, float g);

    @Shadow
    protected abstract void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h, float i);

    @Shadow
    protected abstract void scale(T livingEntity, PoseStack poseStack, float f);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "TAIL"))
    private void filterLayers(T livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntity instanceof LocalPlayer && FirstPersonMode.isFirstPersonPass() && !livingEntity.isSpectator()) {
            poseStack.pushPose();

            float entityScale = livingEntity.getScale();
            poseStack.scale(entityScale, entityScale, entityScale);

            float h = Mth.rotLerp(g, livingEntity.yBodyRotO, livingEntity.yBodyRot);
            float j = Mth.rotLerp(g, livingEntity.yHeadRotO, livingEntity.yHeadRot);
            float k = j - h;

            if (livingEntity.isPassenger() && livingEntity.getVehicle() instanceof LivingEntity livingEntity2) {
                h = Mth.rotLerp(g, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
                k = Mth.wrapDegrees(j - h);
                float l = Mth.wrapDegrees(k);
                if (l < -85.0F) {
                    l = -85.0F;
                }
                if (l >= 85.0F) {
                    l = 85.0F;
                }
                h = j - l;
                if (l * l > 2500.0F) {
                    h += l * 0.2F;
                }
                k = Mth.wrapDegrees(j - h);
            }

            float m = Mth.lerp(g, livingEntity.xRotO, livingEntity.getXRot());
            if (LivingEntityRenderer.isEntityUpsideDown(livingEntity)) {
                m *= -1.0F;
                k *= -1.0F;
            }

            float n = this.getBob(livingEntity, g);

            this.setupRotations(livingEntity, poseStack, n, h, g, entityScale);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(livingEntity, poseStack, g);
            poseStack.translate(0.0F, -1.501F, 0.0F);

            float o = 0.0F;
            float p = 0.0F;
            if (!livingEntity.isPassenger() && livingEntity.isAlive()) {
                o = livingEntity.walkAnimation.speed(g);
                p = livingEntity.walkAnimation.position(g);
                if (livingEntity.isBaby()) {
                    p *= 3.0F;
                }
                if (o > 1.0F) {
                    o = 1.0F;
                }
            }

            for (RenderLayer<T, M> renderLayer : this.layers) {
                if (renderLayer instanceof HumanoidArmorLayer) {
                    renderLayer.render(poseStack, multiBufferSource, i, livingEntity, p, o, g, n, k, m);
                }
            }

            poseStack.popPose();
        }
    }
}
