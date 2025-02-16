package com.github.razorplay01.ismah.util;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.event.GeoRenderEvent;

public class GeckoArmorRenderHandler {
    private GeckoArmorRenderHandler() {
        // []
    }

    public static void register() {
        registerGeoRenderPreEvent();
        registerGeoRenderPostEvent();
    }

    private static void registerGeoRenderPreEvent() {
        GeoRenderEvent.Armor.Pre.EVENT.register(pre -> {
            if (pre.getEntity() instanceof Player player) {
                AnimationApplier emote = ((IAnimatedPlayer) player).playerAnimator_getAnimation();

                if (emote.isActive() && emote.getFirstPersonMode() == FirstPersonMode.THIRD_PERSON_MODEL && FirstPersonMode.isFirstPersonPass()) {
                    boolean showRightArm = emote.getFirstPersonConfiguration().isShowRightArm();
                    boolean showLeftArm = emote.getFirstPersonConfiguration().isShowLeftArm();
                    pre.getModel().topLevelBones().forEach(coreGeoBone -> coreGeoBone.setHidden(true));

                    pre.getModel().topLevelBones().forEach(coreGeoBone -> {
                        if ((showRightArm && isDescendantOf(coreGeoBone, "bipedRightArm")) ||
                                (showLeftArm && isDescendantOf(coreGeoBone, "bipedLeftArm")) ||
                                (showRightArm && isDescendantOf(coreGeoBone, "armorRightArm")) ||
                                (showLeftArm && isDescendantOf(coreGeoBone, "armorLeftArm"))) {
                            coreGeoBone.setHidden(false);
                        }
                    });
                }
            }
            return true;
        });
    }

    private static void registerGeoRenderPostEvent() {
        GeoRenderEvent.Armor.Post.EVENT.register(pre -> {
            if (pre.getEntity() instanceof Player player) {
                AnimationApplier emote = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
                if (!emote.isActive() || emote.getFirstPersonMode() != FirstPersonMode.THIRD_PERSON_MODEL || !FirstPersonMode.isFirstPersonPass()) {
                    pre.getModel().topLevelBones().forEach(coreGeoBone -> coreGeoBone.setHidden(false));
                }
            }
        });
    }

    private static boolean isDescendantOf(GeoBone bone, String ancestorName) {
        if (bone == null) {
            return false;
        }
        if (bone.getName().equalsIgnoreCase(ancestorName)) {
            return true;
        }
        if (bone.getParent() != null) {
            return isDescendantOf(bone.getParent(), ancestorName);
        }
        return false;
    }
}