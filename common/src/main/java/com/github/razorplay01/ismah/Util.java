package com.github.razorplay01.ismah;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class Util {
    public static boolean isValidChestplate(ItemStack item) {
        return item.getItem() instanceof ArmorItem armorItem &&
                armorItem.getEquipmentSlot() == EquipmentSlot.CHEST;
    }
}
