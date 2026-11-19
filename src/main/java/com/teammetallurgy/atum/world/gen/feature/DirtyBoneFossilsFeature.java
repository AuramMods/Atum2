package com.teammetallurgy.atum.world.gen.feature;

import com.mojang.serialization.Codec;
import com.teammetallurgy.atum.Atum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class DirtyBoneFossilsFeature extends Feature<NoneFeatureConfiguration> {
    public DirtyBoneFossilsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunk = new ChunkPos(context.origin());
        Rotation rotation = Rotation.getRandom(random);
        ResourceLocation id = new ResourceLocation(Atum.MOD_ID, "fossils/fossil_" + (1 + random.nextInt(15)));
        var template = level.getLevel().getStructureManager().get(id);
        if (template.isEmpty()) {
            return false;
        }

        StructureTemplate fossil = template.get();
        Vec3i size = fossil.getSize(rotation);
        if (size.getX() > 16 || size.getZ() > 16) {
            return false;
        }

        int x = chunk.getMinBlockX() + random.nextInt(17 - size.getX());
        int z = chunk.getMinBlockZ() + random.nextInt(17 - size.getZ());
        int surface = level.getMaxBuildHeight();
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dz = 0; dz < size.getZ(); dz++) {
                surface = Math.min(surface, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz));
            }
        }

        int y = Math.max(surface - 15 - random.nextInt(10), level.getMinBuildHeight() + 10);
        BlockPos origin = fossil.getZeroPositionWithTransform(new BlockPos(x, y, z), Mirror.NONE, rotation);
        BoundingBox bounds = new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
                chunk.getMaxBlockX(), level.getMaxBuildHeight() - 1, chunk.getMaxBlockZ());
        var settings = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(bounds).setRandom(random)
                .addProcessor(new BlockRotProcessor(0.8F));
        return fossil.placeInWorld(level, origin, origin, settings, random, 4);
    }
}
