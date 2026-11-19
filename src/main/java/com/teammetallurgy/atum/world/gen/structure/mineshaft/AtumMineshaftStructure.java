package com.teammetallurgy.atum.world.gen.structure.mineshaft;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teammetallurgy.atum.init.AtumStructures;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class AtumMineshaftStructure extends Structure {
    public static final Codec<AtumMineshaftStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            settingsCodec(instance), Type.CODEC.fieldOf("variant").forGetter(mineshaft -> mineshaft.variant)
    ).apply(instance, AtumMineshaftStructure::new));

    private final Type variant;

    public AtumMineshaftStructure(StructureSettings settings, Type variant) {
        super(settings);
        this.variant = variant;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        int seaLevel = context.chunkGenerator().getSeaLevel();
        return Optional.of(new GenerationStub(context.chunkPos().getMiddleBlockPosition(seaLevel),
                builder -> buildPieces(context, builder)));
    }

    private void buildPieces(GenerationContext context, StructurePiecesBuilder builder) {
        var generator = context.chunkGenerator();
        var random = context.random();
        ChunkPos chunk = context.chunkPos();
        var room = new AtumMineshaftPieces.Room(0, random, chunk.getMinBlockX() + 2, chunk.getMinBlockZ() + 2, this.variant);
        builder.addPiece(room);
        room.addChildren(room, builder, random);

        if (this.variant.isSurface()) {
            int surface = generator.getBaseHeight(chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(),
                    Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            BoundingBox bounds = builder.getBoundingBox();
            int yOffset = surface - bounds.maxY() + bounds.getYSpan() / 2 + 5;
            builder.offsetPiecesVertically(yOffset);
        } else {
            builder.moveBelowSeaLevel(generator.getSeaLevel(), context.heightAccessor().getMinBuildHeight(), random, 10);
        }
    }

    @Override
    public StructureType<?> type() {
        return AtumStructures.MINESHAFT.get();
    }

    public enum Type implements StringRepresentable {
        DEADWOOD("deadwood", false),
        LIMESTONE("limestone", false),
        DEADWOOD_SURFACE("deadwood_surface", true),
        LIMESTONE_SURFACE("limestone_surface", true);

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private static final Type[] TYPES = values();
        private final String name;
        private final boolean surface;

        Type(String name, boolean surface) {
            this.name = name;
            this.surface = surface;
        }

        public boolean isSurface() {
            return this.surface;
        }

        public static Type byId(int id) {
            return id >= 0 && id < TYPES.length ? TYPES[id] : DEADWOOD;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
