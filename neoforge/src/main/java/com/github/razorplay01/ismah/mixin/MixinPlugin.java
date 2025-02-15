package com.github.razorplay01.ismah.mixin;

import com.google.common.collect.ImmutableMap;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MixinPlugin implements IMixinConfigPlugin {
    private static final Supplier<Boolean> TRUE = () -> true;

    private static final Map<String, Supplier<Boolean>> APPLY_IF_PRESENT = ImmutableMap.of(
            "com.github.razorplay01.ismah.mixin.LivingEntityRendererMixin", () -> isModLoaded("playeranimator"),
            "com.github.razorplay01.ismah.mixin.HumanoidArmorLayerMixin", () -> isModLoaded("playeranimator")
    );

    private static final Map<String, Supplier<Boolean>> APPLY_IF_ABSENT = ImmutableMap.of(
    );

    private static boolean isModLoaded(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    @Override
    public void onLoad(String mixinPackage) {
        // []
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (APPLY_IF_PRESENT.containsKey(mixinClassName)) {
            return APPLY_IF_PRESENT.get(mixinClassName).get();
        }

        if (APPLY_IF_ABSENT.containsKey(mixinClassName)) {
            return APPLY_IF_ABSENT.get(mixinClassName).get();
        }

        return TRUE.get();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // []
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // []
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // []
    }
}