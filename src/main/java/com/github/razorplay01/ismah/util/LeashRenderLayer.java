package com.github.razorplay01.ismah.util;

import com.github.razorplay01.ismah.mixin.accesor.PlayerModelAccesor;
import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import com.github.razorplay01.ismah.util.accessor.LeashStateAccessor;
import com.github.razorplay01.ismah.util.accessor.LivingEntityRenderStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

//? if >=1.21.2{
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
//?}
//? if <1.21.11{
/*import net.minecraft.client.model.PlayerModel;
*///?}
//? if >=1.21.11{
import net.minecraft.client.model.player.PlayerModel;
 //?}

import static com.github.razorplay01.ismah.util.LeashRenderer.*;

//? if <1.21.2{
/*public class LeashRenderLayer<T extends net.minecraft.world.entity.LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public LeashRenderLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	@Override
	public void render(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, int i, T entity, float f, float g, float h, float j, float k, float l) {
		if (!(entity instanceof net.minecraft.client.player.AbstractClientPlayer player)) return;
		if (!(player instanceof LeashStateAccessor leashAccess)) return;
		if (leashAccess.ismah$getLeashState()) {
			PlayerModel model = (PlayerModel) this.getParentModel();
			boolean isRightArm = player.getMainArm() == HumanoidArm.RIGHT;
			boolean isSlim = ((PlayerModelAccesor) model).ismah$getSlim();
			ModelPart leash = isRightArm ? (isSlim ? RIGHT_LEASH_SLIM : RIGHT_LEASH) : (isSlim ? LEFT_LEASH_SLIM : LEFT_LEASH);
			ModelPart arm = isRightArm ? model.rightArm : model.leftArm;

			renderArmLeash(poseStack, multiBufferSource, i, arm, leash);
		}
	}
}
*///?}

//? if >=1.21.2{
public class LeashRenderLayer<T extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public LeashRenderLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

	//? if < 1.21.9 {
	/*@Override
	public void render(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, int light, T entityRenderState, float f, float g) {
		if (!(((LivingEntityRenderStateAccessor) entityRenderState).ismah$getEntity() instanceof Player player)) return;
		if (!(player instanceof net.minecraft.client.player.LocalPlayer localPlayer)) return;
		if (((LeashStateAccessor) localPlayer).ismah$getLeashState()) {
			PlayerModel model = (PlayerModel) this.getParentModel();
			boolean isRightArm = ((HumanoidRenderState) entityRenderState).mainArm == HumanoidArm.RIGHT;
			boolean isSlim = ((PlayerModelAccesor) model).ismah$getSlim();
			ModelPart leash = isRightArm ? (isSlim ? RIGHT_LEASH_SLIM : RIGHT_LEASH) : (isSlim ? LEFT_LEASH_SLIM : LEFT_LEASH);
			ModelPart arm = isRightArm ? model.rightArm : model.leftArm;

			renderArmLeash(poseStack, multiBufferSource, light, arm, leash);
		}
	}
	*///?}

	//? if >= 1.21.9 {
    @Override
    public void submit(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int light, T entityRenderState, float f, float g) {
        if (((LivingEntityRenderStateAccessor) entityRenderState).ismah$getEntity() instanceof Player player && ((LeashStateAccessor) player).ismah$getLeashState()) {
            PlayerModel model = (PlayerModel) this.getParentModel();

            HumanoidArm fpArm = ((FirstPersonArmRenderStateAccessor) entityRenderState).ismah$getFirstPersonArm();
            HumanoidArm targetArm = fpArm != null ? fpArm : ((HumanoidRenderState) entityRenderState).mainArm;

            boolean isRightArm = targetArm == HumanoidArm.RIGHT;
            boolean isSlim = ((PlayerModelAccesor) model).ismah$getSlim();
            ModelPart leash = isRightArm ? (isSlim ? RIGHT_LEASH_SLIM : RIGHT_LEASH) : (isSlim ? LEFT_LEASH_SLIM : LEFT_LEASH);
            ModelPart arm = isRightArm ? model.rightArm : model.leftArm;

            renderArmLeash(poseStack, submitNodeCollector, light, arm, leash);
        }
    }
	//?}
}
//?}
