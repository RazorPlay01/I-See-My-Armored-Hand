package com.github.razorplay01.ismah.api;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for custom armor renderers in first-person view.
 * This class allows mod developers to register their implementations of {@link CustomArmorRenderer}
 * to handle custom armor rendering on the player's arm in first-person perspective.
 */
public class ArmorRendererRegistry {
    private static final List<CustomArmorRenderer> renderers = new ArrayList<>();

    /**
     * Registers a custom armor renderer.
     * Mods should call this method during initialization to add their renderer to the registry.
     * Renderers are checked in the order they are registered, and the first matching renderer
     * for an ItemStack will be used.
     *
     * @param renderer The custom armor renderer to register.
     */
    public static void register(CustomArmorRenderer renderer) {
        renderers.add(renderer);
    }

    /**
     * Retrieves the first registered renderer that can handle the given ItemStack.
     * This method is used internally by the ISMAH mod to determine which renderer to use.
     *
     * @param stack The ItemStack of the equipped armor.
     * @return The first {@link CustomArmorRenderer} that returns {@code true} for {@link CustomArmorRenderer#canRender},
     *         or {@code null} if no renderer matches.
     */
    public static CustomArmorRenderer getRenderer(ItemStack stack) {
        for (CustomArmorRenderer renderer : renderers) {
            if (renderer.canRender(stack)) {
                return renderer;
            }
        }
        return null;
    }
}