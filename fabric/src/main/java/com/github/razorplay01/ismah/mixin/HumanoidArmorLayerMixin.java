package com.github.razorplay01.ismah.mixin;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    protected HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(
            method = "setPartVisibility",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyArmorVisibility(A humanoidModel, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        AnimationApplier emote = ((IAnimatedPlayer) Minecraft.getInstance().player).playerAnimator_getAnimation();
        if (emote.isActive() && emote.getFirstPersonMode() == FirstPersonMode.THIRD_PERSON_MODEL && FirstPersonMode.isFirstPersonPass()) {
            humanoidModel.setAllVisible(false);
            if (equipmentSlot == EquipmentSlot.CHEST) {
                humanoidModel.rightArm.visible = emote.getFirstPersonConfiguration().isShowRightArm();
                humanoidModel.leftArm.visible = emote.getFirstPersonConfiguration().isShowLeftArm();
                humanoidModel.body.visible = false;
            }
            ci.cancel();
        }
    }
}