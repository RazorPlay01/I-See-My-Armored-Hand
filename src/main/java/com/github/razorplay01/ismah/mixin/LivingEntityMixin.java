package com.github.razorplay01.ismah.mixin;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
//? <= 1.21.1 {
/*import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
*///?}
//? >= 1.21.2 {
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import com.github.razorplay01.ismah.util.accessor.RenderStateLivingEntityAccessor;
//?}

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable/*? <= 1.21.1 {*//*, FirstPersonArmRenderStateAccessor*//*?} */ /*? >= 1.21.2 {*/, RenderStateLivingEntityAccessor/*?} */ {
	protected LivingEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	//? <= 1.21.1 {
	/*@Unique
	private HumanoidArm ismah$firstPersonArm;
	@Unique
	private PartPose ismah$firstPersonArmPose;

	@Override
	public HumanoidArm ismah$getFirstPersonArm() {
		return this.ismah$firstPersonArm;
	}

	@Override
	public void ismah$setFirstPersonArm(HumanoidArm arm) {
		this.ismah$firstPersonArm = arm;
	}

	@Override
	public PartPose ismah$getFirstPersonArmPose() {
		return this.ismah$firstPersonArmPose;
	}

	@Override
	public void ismah$setFirstPersonArmPose(PartPose pose) {
		this.ismah$firstPersonArmPose = pose;
	}
	*///?}

	//? >= 1.21.2 {
    @Unique
    private EntityRenderState ismah$renderState;

    @Override
    public EntityRenderState ismah$getRenderState() {
        return this.ismah$renderState;
    }

    @Override
    public void ismah$setRenderState(EntityRenderState renderState) {
        this.ismah$renderState = renderState;
    }
	//?}
}
