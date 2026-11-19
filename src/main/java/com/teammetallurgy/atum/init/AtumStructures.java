package com.teammetallurgy.atum.init;

import com.mojang.serialization.Codec;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.world.gen.structure.girafitomb.GirafiTombStructure;
import com.teammetallurgy.atum.world.gen.structure.pyramid.PyramidStructure;
import com.teammetallurgy.atum.world.gen.structure.ruins.RuinStructure;
import com.teammetallurgy.atum.world.gen.structure.tomb.TombStructure;
import com.teammetallurgy.atum.world.gen.structure.mineshaft.AtumMineshaftStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AtumStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPE_DEFERRED = DeferredRegister.create(Registries.STRUCTURE_TYPE, Atum.MOD_ID);
    public static final RegistryObject<StructureType<GirafiTombStructure>> GIRAFI_TOMB = register("girafi_tomb", GirafiTombStructure.CODEC);
    public static final RegistryObject<StructureType<TombStructure>> TOMB = register("tomb", TombStructure.CODEC);
    public static final RegistryObject<StructureType<RuinStructure>> RUIN = register("ruin", RuinStructure.CODEC);
    public static final RegistryObject<StructureType<PyramidStructure>> PYRAMID = register("pyramid", PyramidStructure.CODEC);

    public static final RegistryObject<StructureType<AtumMineshaftStructure>> MINESHAFT = register("mineshaft", AtumMineshaftStructure.CODEC);

    //Structure Resource Keys (Only used in certain instance, only add if needed)
    public static final ResourceKey<Structure> PYRAMID_KEY = createKey("pyramid");
    public static final ResourceKey<Structure> GENERIC_VILLAGE = createKey("village_generic");

    private static <S extends Structure> RegistryObject<StructureType<S>> register(String name, Codec<S> codec) {
        return STRUCTURE_TYPE_DEFERRED.register(name, () -> structureTypeFromCodec(codec));
    }

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, new ResourceLocation(Atum.MOD_ID, name));
    }

    private static <T extends Structure> StructureType<T> structureTypeFromCodec(Codec<T> structureCodec) {
        return () -> structureCodec;
    }
}
