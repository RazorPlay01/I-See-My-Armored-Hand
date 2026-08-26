package com.github.razorplay01.ismah.mixin;

import com.github.razorplay01.ismah.util.accessor.LeashStateAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

//? if < 1.21.1 {
/*import net.minecraft.world.entity.Mob;
*///?}
//? if >= 1.21.1 {
import net.minecraft.world.entity.Leashable;
//?}

@Mixin(ClientPacketListener.class)
public abstract class LeashAttachMixin {
    @Shadow
    private ClientLevel level;


	//? if < 1.21.1 {
	/*@Inject(method = "handleEntityLinkPacket(Lnet/minecraft/network/protocol/game/ClientboundSetEntityLinkPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;setDelayedLeashHolderId(I)V"))
	*///?}
	//? if >= 1.21.1 {
	@Inject(method = "handleEntityLinkPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;setDelayedLeashHolderId(I)V"))
	//?}
    private void onEntityAttach(ClientboundSetEntityLinkPacket packet, CallbackInfo ci) {
        Entity entity = this.level.getEntity(packet.getSourceId());
        if (!(isLeashable(entity))) return;

		var target = (/*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/) entity;

        Entity currentHolder = getLeashHolder(target);
        int newHolderId = packet.getDestId();

        if (newHolderId == 0) {
            // Se está soltando la correa
            if (currentHolder instanceof AbstractClientPlayer player) {
                tryRemoveLeashState(player, target);
            }
        } else {
            // Se está asignando una nueva correa
            Entity newHolder = level.getEntity(newHolderId);

            if (newHolder instanceof LeashStateAccessor access) {
                access.ismah$setLeashState(true);
            } else if (currentHolder instanceof AbstractClientPlayer player) {
                tryRemoveLeashState(player, target);
            }
        }
    }

    @Unique
    private boolean isLeashable(Entity entity) {
        return entity instanceof /*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/;
    }

    @Unique
    private Entity getLeashHolder(/*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/ leashable) {
        return leashable.getLeashHolder();
    }

    @Unique
    private void tryRemoveLeashState(AbstractClientPlayer player, /*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/ target) {
        if (!hasOtherLeashedEntities(player, target)) {
            if (player instanceof LeashStateAccessor access) {
                access.ismah$setLeashState(false);
            }
        }
    }

    @Unique
    private boolean hasOtherLeashedEntities(AbstractClientPlayer player, /*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/ excluded) {
        AABB box = new AABB(
                player.getX() - 16, player.getY() - 16, player.getZ() - 16,
                player.getX() + 16, player.getY() + 16, player.getZ() + 16
        );

        List<Entity> nearby = player.level().getEntitiesOfClass(Entity.class, box);

        for (Entity entity : nearby) {
            if (entity == excluded) continue;

            if (isLeashable(entity)) {
                var leashable = (/*? if >=1.21.1 {*/ Leashable /*?} else {*/ /*Mob*//*?}*/) entity;
                if (getLeashHolder(leashable) == player) {
                    return true;
                }
            }
        }
        return false;
    }
}
