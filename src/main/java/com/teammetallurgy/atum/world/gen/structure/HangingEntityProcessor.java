package com.teammetallurgy.atum.world.gen.structure;

import com.mojang.serialization.Codec;
import com.teammetallurgy.atum.Atum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class HangingEntityProcessor extends StructureProcessor {
    private static final Codec<HangingEntityProcessor> CODEC = Codec.unit(new HangingEntityProcessor());
    public static final DeferredRegister<StructureProcessorType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Atum.MOD_ID);
    private static final RegistryObject<StructureProcessorType<HangingEntityProcessor>> TYPE =
            TYPES.register("hanging_entity_anchor", () -> () -> CODEC);

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(
            LevelReader level, BlockPos origin,
            StructureTemplate.StructureEntityInfo original, StructureTemplate.StructureEntityInfo placed,
            StructurePlaceSettings settings, StructureTemplate template) {
        CompoundTag nbt = placed.nbt;
        if (!nbt.contains("TileX", Tag.TAG_INT) || !nbt.contains("TileY", Tag.TAG_INT)
                || !nbt.contains("TileZ", Tag.TAG_INT)) {
            return placed;
        }

        // Legacy templates retain the attachment coordinates from the world that recorded them.
        BlockPos anchor = placed.blockPos;
        CompoundTag relocated = nbt.copy();
        relocated.putInt("TileX", anchor.getX());
        relocated.putInt("TileY", anchor.getY());
        relocated.putInt("TileZ", anchor.getZ());
        return new StructureTemplate.StructureEntityInfo(placed.pos, anchor, relocated);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return TYPE.get();
    }
}
