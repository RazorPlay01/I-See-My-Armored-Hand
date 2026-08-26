package com.github.razorplay01.ismah.mixin;
//? >= 1.21.2 {

/*import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import com.github.razorplay01.ismah.util.accessor.LivingEntityRenderStateAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> implements RenderLayerParent<S, M> {

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void addEntityToRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        ((LivingEntityRenderStateAccessor) state).ismah$setEntity(entity);
        if (state instanceof FirstPersonArmRenderStateAccessor access) {
            access.ismah$setFirstPersonArm(null);
            access.ismah$setFirstPersonArmPose(null);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void cacheRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
        ((com.github.razorplay01.ismah.util.accessor.RenderStateLivingEntityAccessor) entity).ismah$setRenderState(state);
    }
}
*///?}
