package com.github.razorplay01.ismah.util;

import com.github.razorplay01.ismah.mixin.accesor.HumanoidArmorLayerAccessor;
import com.github.razorplay01.ismah.util.accessor.FirstPersonArmRenderStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

//? if < 1.21.2 {
/*import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ArmorItem;
*///?}
//? if < 1.21 {
/*import net.minecraft.world.item.DyeableLeatherItem;
*///?}
//? if >= 1.21 && < 1.21.2 {
/*import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.component.DyedItemColor;
*///?}
//? if >= 1.20 && < 1.21.2 {
/*import net.minecraft.core.registries.BuiltInRegistries;
*///?}
//? if < 1.20 {
/*import net.minecraft.core.Registry;
*///?}
//? if < 1.21.2 && forge {
/*import net.minecraftforge.client.ForgeHooksClient;
*///?}
//? if < 1.21.2 && neoforge {
/*import net.neoforged.neoforge.client.ClientHooks;
*///?}
//? if >= 1.21.2 && < 1.21.9 {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
*///?}
//? if >= 1.21.2 && < 1.21.11 {
/*import net.minecraft.client.renderer.RenderType;
*///?}
//? if >= 1.21.11 {
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
//?}
//? if >= 1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
//?}

public final class FirstPersonArmorRenderer {

	// Piezas que componen el peto en mods como Immersive Armors.
	private static final String[] CHEST_PIECES = {"body_upper", "body_middle", "shoulder"};

	private FirstPersonArmorRenderer() {
	}

	// --- visibilidad -------------------------------------------------------

	private static void hide(ModelPart part) {
		part.getAllParts().forEach(child -> child.visible = false);
	}

	private static void show(ModelPart part) {
		part.getAllParts().forEach(child -> child.visible = true);
	}

	public static void restoreAllVisible(HumanoidModel<?> model) {
		model.head.visible = true;
		model.hat.visible = true;
		model.body.visible = true;
		model.rightArm.visible = true;
		model.leftArm.visible = true;
		model.rightLeg.visible = true;
		model.leftLeg.visible = true;
	}

	// Muestra solo el brazo indicado y todos sus descendientes (mangas, hombreras...).
	public static void applyArmOnlyPose(HumanoidModel<?> model, HumanoidArm arm, PartPose armPose) {
		hide(model.head);
		hide(model.hat);
		hide(model.body);
		hide(model.rightArm);
		hide(model.leftArm);
		hide(model.rightLeg);
		hide(model.leftLeg);

		ModelPart armPart = arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
		show(armPart);

		if (armPose != null) {
			armPart.loadPose(armPose);
		}
	}

	// --- utilidades --------------------------------------------------------

	//? if <1.20 {
    /*private static boolean isChestSlot(ArmorItem armorItem) {
        return armorItem.getSlot() == EquipmentSlot.CHEST;
    }

    private static String itemNamespace(ItemStack stack) {
        return Registry.ITEM.getKey(stack.getItem()).getNamespace();
    }
    *///?}
	//? if >=1.20 && <1.21.2 {
    /*private static boolean isChestSlot(ArmorItem armorItem) {
        return armorItem.getEquipmentSlot() == EquipmentSlot.CHEST;
    }

    private static String itemNamespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }
    *///?}

	private static boolean textureExists(Identifier texture) {
		return Minecraft.getInstance().getResourceManager().getResource(texture).isPresent();
	}

	//? if >=1.21 {
	private static Identifier of(String full) {
		return Identifier.parse(full);
	}

	//?}
	//? if <1.21 {
    /*private static Identifier of(String full) {
        return new Identifier(full);
    }
    *///?}
	//? if >=1.21.2 && <1.21.11 {
    /*private static RenderType armorCutout(Identifier texture) {
        return RenderType.armorCutoutNoCull(texture);
    }
    *///?}
	//? if >=1.21.11 {
	private static RenderType armorCutout(Identifier texture) {
		return RenderTypes.armorCutoutNoCull(texture);
	}
	//?}

	private static void addPieceCandidates(List<Identifier> out, String namespace, String path, boolean overlay) {
		for (String piece : CHEST_PIECES) {
			String file = overlay ? piece + "_overlay.png" : piece + ".png";
			out.add(of(String.format("%s:textures/models/armor/%s/%s", namespace, path, file)));
		}
	}

	private static void addChestTextureCandidates(List<Identifier> out, String namespace, String path,
												  boolean overlay) {
		String layerSuffix = overlay ? "_layer_1_overlay.png" : "_layer_1.png";
		String single = overlay ? "_overlay.png" : ".png";
		out.add(of(String.format("%s:textures/models/armor/%s%s", namespace, path, layerSuffix)));
		addPieceCandidates(out, namespace, path, overlay);
		out.add(of(String.format("%s:textures/models/armor/%s%s", namespace, path, single)));
	}

	private static List<Identifier> chestTextureCandidates(Identifier preferred, String itemNamespace,
														   String materialName, boolean overlay) {
		List<Identifier> out = new ArrayList<>();
		if (preferred != null) {
			out.add(preferred);
		}

		String domain = "minecraft";
		String plain = materialName;
		int namespaceEnd = materialName.indexOf(':');
		if (namespaceEnd != -1) {
			domain = materialName.substring(0, namespaceEnd);
			plain = materialName.substring(namespaceEnd + 1);
		}

		addChestTextureCandidates(out, domain, plain, overlay);
		if (!itemNamespace.equals(domain)) {
			addChestTextureCandidates(out, itemNamespace, plain, overlay);
		}
		return out;
	}

	//? if <1.21 {
    /*private static boolean renderAllExisting(HumanoidModel<?> model, PoseStack poseStack, MultiBufferSource buffers,
            int light, List<Identifier> candidates, float red, float green, float blue) {
        boolean rendered = false;
        for (Identifier candidate : candidates) {
            if (!textureExists(candidate)) {
                continue;
            }
            VertexConsumer consumer = buffers.getBuffer(RenderType.armorCutoutNoCull(candidate));
            model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
            rendered = true;
        }
        return rendered;
    }
    *///?}
	//? if >=1.21 && <1.21.2 {
    /*private static boolean renderAllExisting(HumanoidModel<?> model, PoseStack poseStack, MultiBufferSource buffers,
            int light, List<Identifier> candidates, int tint) {
        boolean rendered = false;
        for (Identifier candidate : candidates) {
            if (!textureExists(candidate)) {
                continue;
            }
            VertexConsumer consumer = buffers.getBuffer(RenderType.armorCutoutNoCull(candidate));
            model.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, tint);
            rendered = true;
        }
        return rendered;
    }
    *///?}

	// --- render por version ------------------------------------------------

	//? if <1.21 && fabric {
    /*public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
            MultiBufferSource buffers, int light, AbstractClientPlayer player) {
        FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) player;
        HumanoidArm arm = fp.ismah$getFirstPersonArm();
        if (arm == null) {
            return;
        }
        PartPose armPose = fp.ismah$getFirstPersonArmPose();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ArmorItem armorItem) || !isChestSlot(armorItem)) {
            return;
        }

        HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
        HumanoidModel<?> model = accessor.ismah$outerModel();
        applyArmOnlyPose(model, arm, armPose);

        String materialName = armorItem.getMaterial().getName();
        String namespace = itemNamespace(chest);
        List<Identifier> baseCandidates = chestTextureCandidates(null, namespace, materialName, false);

        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        boolean dyeable = chest.getItem() instanceof DyeableLeatherItem;
        if (dyeable) {
            int color = ((DyeableLeatherItem) chest.getItem()).getColor(chest);
            red = (color >> 16 & 255) / 255.0F;
            green = (color >> 8 & 255) / 255.0F;
            blue = (color & 255) / 255.0F;
        }
        boolean rendered = renderAllExisting(model, poseStack, buffers, light, baseCandidates, red, green, blue);
        if (rendered && dyeable) {
            List<Identifier> overlayCandidates = chestTextureCandidates(null, namespace, materialName, true);
            renderAllExisting(model, poseStack, buffers, light, overlayCandidates, 1.0F, 1.0F, 1.0F);
        }
        if (rendered && chest.hasFoil()) {
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light,
                    OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    *///?}

	//? if <1.21 && forge {
    /*public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
            MultiBufferSource buffers, int light, AbstractClientPlayer player) {
        FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) player;
        HumanoidArm arm = fp.ismah$getFirstPersonArm();
        if (arm == null) {
            return;
        }
        PartPose armPose = fp.ismah$getFirstPersonArmPose();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ArmorItem armorItem) || !isChestSlot(armorItem)) {
            return;
        }

        HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
        HumanoidModel<?> model = accessor.ismah$outerModel();
        Model hooked = ForgeHooksClient.getArmorModel(player, chest, EquipmentSlot.CHEST, model);
        if (hooked instanceof HumanoidModel<?> humanoidHooked) {
            model = humanoidHooked;
        }
        applyArmOnlyPose(model, arm, armPose);

        String materialName = armorItem.getMaterial().getName();
        String namespace = itemNamespace(chest);
        ResourceLocation preferred = armorLayer.getArmorResource(player, chest, EquipmentSlot.CHEST, null);
        List<Identifier> baseCandidates = chestTextureCandidates(preferred, namespace, materialName, false);

        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        boolean dyeable = chest.getItem() instanceof DyeableLeatherItem;
        if (dyeable) {
            int color = ((DyeableLeatherItem) chest.getItem()).getColor(chest);
            red = (color >> 16 & 255) / 255.0F;
            green = (color >> 8 & 255) / 255.0F;
            blue = (color & 255) / 255.0F;
        }
        boolean rendered = renderAllExisting(model, poseStack, buffers, light, baseCandidates, red, green, blue);
        if (rendered && dyeable) {
            ResourceLocation preferredOverlay = armorLayer.getArmorResource(player, chest, EquipmentSlot.CHEST, "overlay");
            List<Identifier> overlayCandidates = chestTextureCandidates(preferredOverlay, namespace, materialName, true);
            renderAllExisting(model, poseStack, buffers, light, overlayCandidates, 1.0F, 1.0F, 1.0F);
        }
        if (rendered && chest.hasFoil()) {
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light,
                    OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    *///?}

	//? if >=1.21 && <1.21.2 && fabric {
    /*public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
            MultiBufferSource buffers, int light, AbstractClientPlayer player) {
        FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) player;
        HumanoidArm arm = fp.ismah$getFirstPersonArm();
        if (arm == null) {
            return;
        }
        PartPose armPose = fp.ismah$getFirstPersonArmPose();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ArmorItem armorItem) || !isChestSlot(armorItem)) {
            return;
        }

        HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
        HumanoidModel<?> model = accessor.ismah$outerModel();
        applyArmOnlyPose(model, arm, armPose);

        ArmorMaterial material = armorItem.getMaterial().value();
        int dyeColor = chest.is(ItemTags.DYEABLE)
                ? ARGB32.opaque(DyedItemColor.getOrDefault(chest, -6265536))
                : -1;
        boolean rendered = false;
        for (ArmorMaterial.Layer layer : material.layers()) {
            Identifier texture = layer.texture(false);
            if (!textureExists(texture)) {
                continue;
            }
            int tint = layer.dyeable() ? dyeColor : -1;
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)), light,
                    OverlayTexture.NO_OVERLAY, tint);
            rendered = true;
        }
        if (!rendered) {
            List<Identifier> baseCandidates = new ArrayList<>();
            Identifier materialKey = BuiltInRegistries.ARMOR_MATERIAL.getKey(material);
            if (materialKey != null) {
                addChestTextureCandidates(baseCandidates, materialKey.getNamespace(), materialKey.getPath(), false);
            }
            rendered = renderAllExisting(model, poseStack, buffers, light, baseCandidates, dyeColor);
            if (rendered && chest.is(ItemTags.DYEABLE)) {
                List<Identifier> overlayCandidates = new ArrayList<>();
                if (materialKey != null) {
                    addChestTextureCandidates(overlayCandidates, materialKey.getNamespace(), materialKey.getPath(),
                            true);
                }
                renderAllExisting(model, poseStack, buffers, light, overlayCandidates, -1);
            }
        }
        if (rendered && chest.hasFoil()) {
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
    *///?}

	//? if >=1.21 && <1.21.2 && neoforge {
    /*public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
            MultiBufferSource buffers, int light, AbstractClientPlayer player) {
        FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) player;
        HumanoidArm arm = fp.ismah$getFirstPersonArm();
        if (arm == null) {
            return;
        }
        PartPose armPose = fp.ismah$getFirstPersonArmPose();

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ArmorItem armorItem) || !isChestSlot(armorItem)) {
            return;
        }

        HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
        HumanoidModel<?> model = accessor.ismah$outerModel();
        Model hooked = ClientHooks.getArmorModel(player, chest, EquipmentSlot.CHEST, model);
        if (hooked instanceof HumanoidModel<?> humanoidHooked) {
            model = humanoidHooked;
        }
        applyArmOnlyPose(model, arm, armPose);

        ArmorMaterial material = armorItem.getMaterial().value();
        int dyeColor = chest.is(ItemTags.DYEABLE)
                ? ARGB32.opaque(DyedItemColor.getOrDefault(chest, -6265536))
                : -1;
        boolean rendered = false;
        for (ArmorMaterial.Layer layer : material.layers()) {
            Identifier texture = layer.texture(false);
            if (!textureExists(texture)) {
                continue;
            }
            int tint = layer.dyeable() ? dyeColor : -1;
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)), light,
                    OverlayTexture.NO_OVERLAY, tint);
            rendered = true;
        }
        if (!rendered) {
            List<Identifier> baseCandidates = new ArrayList<>();
            Identifier materialKey = BuiltInRegistries.ARMOR_MATERIAL.getKey(material);
            if (materialKey != null) {
                addChestTextureCandidates(baseCandidates, materialKey.getNamespace(), materialKey.getPath(), false);
            }
            rendered = renderAllExisting(model, poseStack, buffers, light, baseCandidates, dyeColor);
            if (rendered && chest.is(ItemTags.DYEABLE)) {
                List<Identifier> overlayCandidates = new ArrayList<>();
                if (materialKey != null) {
                    addChestTextureCandidates(overlayCandidates, materialKey.getNamespace(), materialKey.getPath(),
                            true);
                }
                renderAllExisting(model, poseStack, buffers, light, overlayCandidates, -1);
            }
        }
        if (rendered && chest.hasFoil()) {
            model.renderToBuffer(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
    *///?}

	//? if >=1.21.2 && <1.21.9 {
    /*public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
            MultiBufferSource buffers, int light, HumanoidRenderState state) {
        FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) state;
        HumanoidArm arm = fp.ismah$getFirstPersonArm();
        if (arm == null) {
            return;
        }
        PartPose armPose = fp.ismah$getFirstPersonArmPose();

        ItemStack chest = state.chestEquipment;
        if (!HumanoidArmorLayer.shouldRender(chest, EquipmentSlot.CHEST)) {
            return;
        }

        Equippable equippable = chest.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }

        HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
        HumanoidModel<?> model = state.isBaby ? accessor.ismah$outerModelBaby() : accessor.ismah$outerModel();
        applyArmOnlyPose(model, arm, armPose);

        ResourceKey<EquipmentAsset> assetId = equippable.assetId().get();
        accessor.ismah$equipmentRenderer().renderLayers(EquipmentClientInfo.LayerType.HUMANOID, assetId, model,
                chest, poseStack, buffers, light, (Identifier) null);
        renderPieceExtras(model, poseStack, buffers, light, assetId.identifier().getNamespace(),
                assetId.identifier().getPath());
    }

    private static void renderPieceExtras(HumanoidModel<?> model, PoseStack poseStack, MultiBufferSource buffers,
            int light, String namespace, String path) {
        List<Identifier> extras = new ArrayList<>();
        addPieceCandidates(extras, namespace, path, false);
        for (Identifier texture : extras) {
            if (!textureExists(texture)) {
                continue;
            }
            model.renderToBuffer(poseStack, buffers.getBuffer(armorCutout(texture)), light,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
    *///?}

	//? if >=1.21.9 {
	public static void renderChestArm(HumanoidArmorLayer<?, ?, ?> armorLayer, PoseStack poseStack,
									  SubmitNodeCollector collector, int light, AvatarRenderState state) {
		FirstPersonArmRenderStateAccessor fp = (FirstPersonArmRenderStateAccessor) state;
		HumanoidArm arm = fp.ismah$getFirstPersonArm();
		if (arm == null) {
			return;
		}
		PartPose armPose = fp.ismah$getFirstPersonArmPose();

		ItemStack chest = state.chestEquipment;
		if (!HumanoidArmorLayer.shouldRender(chest, EquipmentSlot.CHEST)) {
			return;
		}

		Equippable equippable = chest.get(DataComponents.EQUIPPABLE);
		if (equippable == null || equippable.assetId().isEmpty()) {
			return;
		}

		HumanoidArmorLayerAccessor accessor = (HumanoidArmorLayerAccessor) armorLayer;
		ArmorModelSet<? extends HumanoidModel<?>> modelSet = state.isBaby
				? accessor.ismah$babyModelSet()
				: accessor.ismah$modelSet();
		HumanoidModel<?> model = modelSet.get(EquipmentSlot.CHEST);
		applyArmOnlyPose(model, arm, armPose);

		ResourceKey<EquipmentAsset> assetId = equippable.assetId().get();
		accessor.ismah$equipmentRenderer().renderLayers(EquipmentClientInfo.LayerType.HUMANOID, assetId,
				(Model<? super AvatarRenderState>) model, state, chest, poseStack, collector, light,
				state.outlineColor);

		List<Identifier> extras = new ArrayList<>();
		addPieceCandidates(extras, assetId.identifier().getNamespace(), assetId.identifier().getPath(), false);
		for (Identifier texture : extras) {
			if (!textureExists(texture)) {
				continue;
			}
			collector.submitModel((Model<? super AvatarRenderState>) model, state, poseStack, armorCutout(texture),
					light, OverlayTexture.NO_OVERLAY, -1/*? <26.3 {*/, null, state.outlineColor, null/*?} */);
		}
	}
	//?}
}
