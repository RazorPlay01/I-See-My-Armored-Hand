package com.github.razorplay01.ismah.mixin;
//? >= 1.21.2 {

import com.github.razorplay01.ismah.util.accessor.LivingEntityRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements LivingEntityRenderStateAccessor {
    @Unique
    private Entity ismah$entity;

    @Override
    public void ismah$setEntity(Entity entity) {
        this.ismah$entity = entity;
    }

    @Override
    public Entity ismah$getEntity() {
        return this.ismah$entity;
    }
}
//?}
