package com.teammetallurgy.atum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;

public class TefnutsCallModel extends Model {
    private final ModelPart spear;

    public TefnutsCallModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.spear = root.getChild("spear");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, 2.0F, -0.5F, 1.0F, 25.0F, 1.0F)
                .texOffs(12, 0).addBox(-2.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F)
                .texOffs(4, 0).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 4.0F, 1.0F)
                .texOffs(16, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F)
                .texOffs(20, 0).mirror().addBox(1.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.spear.render(poses, vertices, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
