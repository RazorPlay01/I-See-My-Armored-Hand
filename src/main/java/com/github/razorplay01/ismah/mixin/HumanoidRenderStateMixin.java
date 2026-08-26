package com.github.razorplay01.ismah.mixin;
//? >= 1.21.2 {

/*import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public abstract class HumanoidRenderStateMixin implements FirstPersonArmRenderStateAccessor {
    @Unique
    private HumanoidArm ismah$firstPersonArm;
    @Unique
    private PartPose ismah$firstPersonArmPose;

    @Override
    public @Nullable HumanoidArm ismah$getFirstPersonArm() {
        return this.ismah$firstPersonArm;
    }

    @Override
    public void ismah$setFirstPersonArm(@Nullable HumanoidArm arm) {
        this.ismah$firstPersonArm = arm;
    }

    @Override
    public @Nullable PartPose ismah$getFirstPersonArmPose() {
        return this.ismah$firstPersonArmPose;
    }

    @Override
    public void ismah$setFirstPersonArmPose(@Nullable PartPose pose) {
        this.ismah$firstPersonArmPose = pose;
    }
}
*///?}
