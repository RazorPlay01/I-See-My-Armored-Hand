package com.github.razorplay01.ismah.util.accessor;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.HumanoidArm;

public interface FirstPersonArmRenderStateAccessor {
	HumanoidArm ismah$getFirstPersonArm();

	void ismah$setFirstPersonArm(HumanoidArm arm);

	PartPose ismah$getFirstPersonArmPose();

	void ismah$setFirstPersonArmPose(PartPose pose);
}
