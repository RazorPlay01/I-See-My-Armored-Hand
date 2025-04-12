package com.github.razorplay01.ismah.compat;

import com.github.L_Ender.cataclysm.client.model.CMModelLayers;
import com.github.L_Ender.cataclysm.client.model.armor.*;
import com.github.L_Ender.cataclysm.items.*;
import com.github.razorplay01.ismah.api.CustomArmorRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CataclysmCompat implements CustomArmorRenderer {
    @Override
    public boolean canRender(ItemStack stack) {
        return stack.getItem() instanceof Bone_Reptile_Armor ||
                stack.getItem() instanceof Bloom_Stone_Pauldrons ||
                stack.getItem() instanceof Cursium_Armor ||
                stack.getItem() instanceof Ignitium_Armor ||
                stack.getItem() instanceof Ignitium_Elytra_ChestPlate;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource vertexConsumers, int light, ItemStack stack, HumanoidArm arm, HumanoidModel<@NotNull LivingEntity> playerModel) {
        if (stack.getItem() instanceof Bone_Reptile_Armor) {
            renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                    new Bone_Reptile_Armor_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.BONE_REPTILE_ARMOR_MODEL)),
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/armor/bone_reptile_armor.png"));
        } else if (stack.getItem() instanceof Bloom_Stone_Pauldrons) {
            renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                    new Bloom_Stone_Pauldrons_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.BLOOM_STONE_PAULDRONS_MODEL)),
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/armor/bloom_stone_pauldrons.png"));
        } else if (stack.getItem() instanceof Cursium_Armor) {
            renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                    new Cursium_Armor_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.CURSIUM_ARMOR_MODEL)),
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/armor/cursium_armor.png"));
        } else if (stack.getItem() instanceof Ignitium_Armor) {
            renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                    new Ignitium_Armor_Model(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.IGNITIUM_ARMOR_MODEL)),
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/armor/ignitium_armor.png"));
        } else if (stack.getItem() instanceof Ignitium_Elytra_ChestPlate) {
            renderArmor(poseStack, vertexConsumers, light, stack, arm, playerModel,
                    new Ignitium_Elytra_chestplate_Model<>(Minecraft.getInstance().getEntityModels().bakeLayer(CMModelLayers.ELYTRA_ARMOR)),
                    ResourceLocation.fromNamespaceAndPath("cataclysm", "textures/armor/ignitium_elytra_chestplate.png"));
        }
    }
}
