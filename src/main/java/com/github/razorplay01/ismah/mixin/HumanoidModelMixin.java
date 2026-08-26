package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >= 1.21.2 {
/*import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
*///?}

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin
		//? if < 1.21.2 {
		<T extends LivingEntity>
		//?}
		//? if >= 1.21.2 {
		/*<T extends HumanoidRenderState>
		*///?}
{

    @Shadow public ModelPart head;
    @Shadow public ModelPart hat;
    @Shadow public ModelPart body;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftArm;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart leftLeg;

	//? if < 1.21.2 {
	@Inject(
			method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
			at = @At("RETURN")
	)
	private void applyFirstPersonArmorPose(T state, float f, float g, float h, float i, float j, CallbackInfo ci) {
	//?}
	//? if >= 1.21.2 {
    /*@Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("RETURN")
    )
    private void applyFirstPersonArmorPose(T state, CallbackInfo ci) {
    
	*///?}
		FirstPersonArmRenderStateAccessor access = (FirstPersonArmRenderStateAccessor) state;
        HumanoidArm arm = access.ismah$getFirstPersonArm();
        PartPose armPose = access.ismah$getFirstPersonArmPose();

        if (arm == null || armPose == null) {
            // Restore visibility for normal rendering
            this.head.visible = true;
            this.hat.visible = true;
            this.body.visible = true;
            this.rightArm.visible = true;
            this.leftArm.visible = true;
            this.rightLeg.visible = true;
            this.leftLeg.visible = true;
            return;
        }

        this.head.visible = false;
        this.hat.visible = false;
        this.body.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        boolean showLeftArm = arm == HumanoidArm.LEFT;
        this.leftArm.visible = showLeftArm;
        this.rightArm.visible = !showLeftArm;

        ModelPart visibleArm = showLeftArm ? this.leftArm : this.rightArm;
        visibleArm.loadPose(armPose);
    }
}
