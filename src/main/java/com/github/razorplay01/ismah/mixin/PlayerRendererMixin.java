package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.config.FirstPersonRenderConfig;
import com.github.razorplay01.ismah.mixin.accesor.LivingEntityRendererAccesor;
import com.github.razorplay01.ismah.util.LeashRenderLayer;
import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.2 && <1.21.9{
/*import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
*///?}

//? if <1.21.9{
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
		//?}

//? if >=1.21.9{
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
*///?}

//? if <1.21.11{
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.RenderType;
//?}
//? if >=1.21.11{
/*import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.model.player.PlayerModel;
*///?}

@Mixin(/*? >=1.21.9 {*//*AvatarRenderer*//*? } else { */ PlayerRenderer /*? } */.class)
public abstract class PlayerRendererMixin
//? < 1.21.2 {
extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
//?} >= 1.21.2 && < 1.21.9 {
/*extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel>
*///?} >= 1.21.11 {
/*<AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel>
*///?}
{
    @Unique
    private HumanoidArmorLayer armorLayer;
    @Unique
    private ArrowLayer arrowLayer;
    @Unique
    private LeashRenderLayer leashRenderLayer;

    protected PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float shadowRadius) {
        super(context, entityModel, shadowRadius);
    }

	//? < 1.21.2 {
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"))
	private boolean captureLayers(PlayerRenderer instance, RenderLayer renderLayer, Operation<Boolean> original) {
	//?} >= 1.21.2 && < 1.21.9 {
	/*@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"))
	private boolean captureLayers(PlayerRenderer instance, RenderLayer renderLayer, Operation<Boolean> original) {
	*///?} >= 1.21.11 {
	/*@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;addLayer(Lnet/minecraft/client/renderer/entity/layers/RenderLayer;)Z"))
	private boolean captureLayers(AvatarRenderer<?> instance, RenderLayer<AvatarRenderState, PlayerModel> renderLayer, Operation<Boolean> original) {
	*///?}
		if (renderLayer instanceof HumanoidArmorLayer armor)
			this.armorLayer = armor;
		if (renderLayer instanceof ArrowLayer arrow)
			this.arrowLayer = arrow;
		return original.call(instance, renderLayer);
	}

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initLeashLayer(EntityRendererProvider.Context context, boolean bl, CallbackInfo ci) {
        this.leashRenderLayer = new LeashRenderLayer<>((/*? >=1.21.9 {*//*AvatarRenderer*//*? } else { */ PlayerRenderer /*? } */) (Object) this);
		((LivingEntityRendererAccesor) this).ismah$addLayer(this.leashRenderLayer);
    }

	//? <1.21.2 {
	@WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private void renderFirstPersonLayers(ModelPart modelPart, PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int light, int overlay, Operation<Void> original, @Local(ordinal = 0) net.minecraft.client.renderer.MultiBufferSource instance, @Local(ordinal = 0) AbstractClientPlayer renderState) {
	//?} >=1.21.2 && <1.21.9 {
	/*@WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
	private void renderFirstPersonLayers(ModelPart modelPart, PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int light, int overlay, Operation<Void> original, @Local(ordinal = 0) net.minecraft.client.renderer.MultiBufferSource instance) {
		*///?} >= 1.21.9 && <1.21.11 {
	/*@WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
	private void renderFirstPersonLayers(SubmitNodeCollector instance, ModelPart modelPart, PoseStack poseStack, RenderType renderType, int light, int overlay, TextureAtlasSprite textureAtlasSprite, Operation<Void> original) {
	 */
		//?} >= 1.21.11 {
	/*@WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModelPart(Lnet/minecraft/client/model/geom/ModelPart;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
	private void renderFirstPersonLayers(SubmitNodeCollector instance, ModelPart modelPart, PoseStack poseStack, RenderType renderType, int light, int overlay, TextureAtlasSprite textureAtlasSprite, Operation<Void> original) {
	*///?}
        if (modelPart != this.model.leftArm && modelPart != this.model.rightArm) {
            //? < 1.21.9 {
			original.call(modelPart, poseStack, vertexConsumer, light, overlay);
			//?} >= 1.21.9 {
			/*original.call(instance, modelPart, poseStack, renderType, light, overlay, textureAtlasSprite);
			*///?}
            return;
        }

        HumanoidArm arm = modelPart == this.model.leftArm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;

        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null) {
            //? < 1.21.9 {
			original.call(modelPart, poseStack, vertexConsumer, light, overlay);
			//?} >= 1.21.9 {
			/*original.call(instance, modelPart, poseStack, renderType, light, overlay, textureAtlasSprite);
			*///?}
            return;
        }

        if (!player.isInvisible()) {
            //? < 1.21.9 {
			original.call(modelPart, poseStack, vertexConsumer, light, overlay);
			//?} >= 1.21.9 {
			/*original.call(instance, modelPart, poseStack, renderType, light, overlay, textureAtlasSprite);
			*///?}
        }

		//? if >= 1.21.2 {
        /*com.github.razorplay01.ismah.util.accessor.RenderStateLivingEntityAccessor accessor = (com.github.razorplay01.ismah.util.accessor.RenderStateLivingEntityAccessor) player;
        if (accessor.ismah$getRenderState() == null) {
            return;
        }
		*///?}

        if (this.leashRenderLayer != null && FirstPersonRenderConfig.isLeashEnabled()) {
			//? < 1.21.2 {
			AbstractClientPlayer tempState = renderState;
			//?} >= 1.21.2 && < 1.21.9 {
			/*PlayerRenderState tempState = new PlayerRenderState();
			this.extractRenderState(player, tempState, 0f);
			*///?} >= 1.21.11 {
			/*AvatarRenderState tempState = new AvatarRenderState();
			this.extractRenderState((AvatarlikeEntity) player, tempState, 0f);
			*///?}

            FirstPersonArmRenderStateAccessor firstPersonState = (FirstPersonArmRenderStateAccessor) tempState;
            firstPersonState.ismah$setFirstPersonArm(arm);
            firstPersonState.ismah$setFirstPersonArmPose(modelPart.storePose());

			//? < 1.21.2 {
			this.leashRenderLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (AbstractClientPlayer) tempState, 0f, 0f, 0f, 0f, 0f, 0f);
			 //?} >= 1.21.2 && < 1.21.9 {
			/*this.leashRenderLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (PlayerRenderState) tempState, 0f, 0f);
			*///?} >= 1.21.11 {
			/*this.leashRenderLayer.submit(poseStack, (SubmitNodeCollector) instance, light, (AvatarRenderState) tempState, 0f, 0f);
			 *///?}
        }

        if (this.armorLayer != null && FirstPersonRenderConfig.isArmorEnabled()) {
			//? < 1.21.2 {
			AbstractClientPlayer tempState = renderState;
			//?} >= 1.21.2 && < 1.21.9 {
			/*PlayerRenderState tempState = new PlayerRenderState();
			this.extractRenderState(player, tempState, 0f);
			*///?} >= 1.21.11 {
			/*AvatarRenderState tempState = new AvatarRenderState();
			this.extractRenderState((AvatarlikeEntity) player, tempState, 0f);
			*///?}

            FirstPersonArmRenderStateAccessor firstPersonState = (FirstPersonArmRenderStateAccessor) tempState;
            firstPersonState.ismah$setFirstPersonArm(arm);
            firstPersonState.ismah$setFirstPersonArmPose(modelPart.storePose());

			//? < 1.21.2 {
			this.armorLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (AbstractClientPlayer) tempState, 0f, 0f, 0f, 0f, 0f, 0f);
			 //?} >= 1.21.2 && < 1.21.9 {
			/*this.armorLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (PlayerRenderState) tempState, 0f, 0f);
			*///?} >= 1.21.11 {
			/*this.armorLayer.submit(poseStack, (SubmitNodeCollector) instance, light, (AvatarRenderState) tempState, 0f, 0f);
			 *///?}
        }

        if (this.arrowLayer != null && FirstPersonRenderConfig.isArrowsEnabled()) {
			//? < 1.21.2 {
			AbstractClientPlayer tempState = renderState;
			//?} >= 1.21.2 && < 1.21.9 {
			/*PlayerRenderState tempState = new PlayerRenderState();
            this.extractRenderState(player, tempState, 0f);
			*///?} >= 1.21.11 {
			/*AvatarRenderState tempState = new AvatarRenderState();
			this.extractRenderState((AvatarlikeEntity) player, tempState, 0f);
			*///?}

            FirstPersonArmRenderStateAccessor firstPersonState = (FirstPersonArmRenderStateAccessor) tempState;
            firstPersonState.ismah$setFirstPersonArm(arm);
            firstPersonState.ismah$setFirstPersonArmPose(modelPart.storePose());

			//? < 1.21.2 {
			this.arrowLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (AbstractClientPlayer) tempState, 0f, 0f, 0f, 0f, 0f, 0f);
			 //?} >= 1.21.2 && < 1.21.9 {
			/*this.arrowLayer.render(poseStack, (net.minecraft.client.renderer.MultiBufferSource) instance, light, (PlayerRenderState) tempState, 0f, 0f);
			*///?} >= 1.21.11 {
			/*this.arrowLayer.submit(poseStack, (SubmitNodeCollector) instance, light, (AvatarRenderState) tempState, 0f, 0f);
			 *///?}
        }

        //? < 1.21.2 {
        // En estas versiones no hay render states: el flag de primera persona vive en la
        // entidad real del jugador. Si no se limpia, HumanoidModelMixin sigue ocultando
        // cabeza/cuerpo/piernas y sobrescribiendo la pose en TODOS los renders posteriores
        // (tercera persona, inventario), dejando el modelo invisible y congelado.
        ((FirstPersonArmRenderStateAccessor) renderState).ismah$setFirstPersonArm(null);
        ((FirstPersonArmRenderStateAccessor) renderState).ismah$setFirstPersonArmPose(null);
        //?}
    }
}
