package com.github.razorplay01.ismah.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class LeashRenderer {
	private LeashRenderer() {
		// []
	}

	private static final Identifier TEXTURE =
			//? < 1.21 {
			/*new
			 *///?}
			Identifier
					//? >= 1.21 {
					.parse
					//?}
					//? < 26 {
					/*("textures/entity/lead_knot.png");
					*///?}
					//? >= 26 {
					("textures/entity/lead_knot/lead_knot.png");
					//?}

	public static final ModelPart RIGHT_LEASH = buildModel(
			"leads$right_leash",
			CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -3.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
	);

	public static final ModelPart RIGHT_LEASH_SLIM = buildModel(
			"leads$right_leash_slim",
			CubeListBuilder.create().texOffs(0, 0).addBox(2.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
	);

	public static final ModelPart LEFT_LEASH = buildModel(
			"leads$left_leash",
			CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -3.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
	);

	public static final ModelPart LEFT_LEASH_SLIM = buildModel(
			"leads$left_leash",
			CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -3.0F, -3.0F, 5.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
	);

	public static ModelPart buildModel(String name, CubeListBuilder builder) {
		MeshDefinition modelData = new MeshDefinition();
		PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild(name, builder, PartPose.offset(-5.0F, 2.0F, 0.0F));
		return LayerDefinition.create(modelData, 32, 32).bakeRoot();
	}

	//? if < 1.21.9 {
	/*public static void renderArmLeash(PoseStack matrices, net.minecraft.client.renderer.MultiBufferSource multiBufferSource, int light, ModelPart arm, ModelPart leash) {
		leash.x = arm.x;
		leash.y = arm.y;
		leash.z = arm.z;
		leash.xRot = arm.xRot;
		leash.yRot = arm.yRot;
		leash.zRot = arm.zRot;
		leash.xScale = arm.xScale;
		leash.yScale = arm.yScale;
		leash.zScale = arm.zScale;

		leash.render(matrices, multiBufferSource.getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
	}
	*///?}

	//? if >= 1.21.9 {
	public static void renderArmLeash(PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int light, ModelPart arm, ModelPart leash) {
		leash.x = arm.x;
		leash.y = arm.y;
		leash.z = arm.z;
		leash.xRot = arm.xRot;
		leash.yRot = arm.yRot;
		leash.zRot = arm.zRot;
		leash.xScale = arm.xScale;
		leash.yScale = arm.yScale;
		leash.zScale = arm.zScale;


		submitNodeCollector.submitModelPart(
				leash,
				matrices,
				/*? if <=1.21.10 {*//*net.minecraft.client.renderer.RenderType*//*?} else {*/ net.minecraft.client.renderer.rendertype.RenderTypes/*?}*/.entitySolid(TEXTURE),
				light,
				OverlayTexture.NO_OVERLAY,
				null);
	}
	//?}
}
