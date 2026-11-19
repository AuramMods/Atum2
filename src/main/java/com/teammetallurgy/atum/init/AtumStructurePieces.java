package com.teammetallurgy.atum.init;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.world.gen.structure.girafitomb.GirafiTombPieces;
import com.teammetallurgy.atum.world.gen.structure.girafitomb.GirafiTombStructure;
import com.teammetallurgy.atum.world.gen.structure.pyramid.PyramidPieces;
import com.teammetallurgy.atum.world.gen.structure.pyramid.PyramidStructure;
import com.teammetallurgy.atum.world.gen.structure.ruins.RuinPieces;
import com.teammetallurgy.atum.world.gen.structure.tomb.TombPieces;
import com.teammetallurgy.atum.world.gen.structure.tomb.TombStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class AtumStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES_DEFERRED = DeferredRegister.create(Registries.STRUCTURE_PIECE, Atum.MOD_ID);
    public static final RegistryObject<StructurePieceType> GIRAFI_TOMB = registerTemplate("girafi_tomb_template", "girafi_tomb",
            GirafiTombPieces.GirafiTombTemplate::new, data -> GirafiTombStructure.GIRAFI_TOMB);
    public static final RegistryObject<StructurePieceType> TOMB = registerTemplate("tomb_template", "tomb",
            TombPieces.TombTemplate::new, data -> TombStructure.TOMB);
    public static final RegistryObject<StructurePieceType> RUIN = registerTemplate("ruin_template", "ruin",
            RuinPieces.RuinTemplate::new, data -> new ResourceLocation(Atum.MOD_ID, "ruins/ruin" + data.getInt("Type")));
    public static final RegistryObject<StructurePieceType> PYRAMID = registerTemplate("pyramid_template", "pyramid",
            PyramidPieces.PyramidTemplate::new, data -> PyramidStructure.PYRAMID);
    public static final RegistryObject<StructurePieceType> PYRAMID_MAZE = register((manager, nbt) -> new PyramidPieces.Maze(nbt), "maze");
    //public static final StructurePieceType MINESHAFT_CORRIDOR = register(AtumMineshaftPieces.Corridor::new, "mineshaft_corridor");
    //public static final StructurePieceType MINESHAFT_CROSSING = register(AtumMineshaftPieces.Cross::new, "mineshaft_crossing");
    //public static final StructurePieceType MINESHAFT_ROOM = register(AtumMineshaftPieces.Room::new, "mineshaft_room");
    //public static final StructurePieceType MINESHAFT_STAIRS = register(AtumMineshaftPieces.Stairs::new, "mineshaft_stairs");

    static {
        register((manager, nbt) -> new PyramidPieces.Maze(nbt), "pyramid_maze");
    }

    private static RegistryObject<StructurePieceType> registerTemplate(String name, String alias,
            StructurePieceType.StructureTemplateType type, Function<CompoundTag, ResourceLocation> legacyTemplate) {
        StructurePieceType.StructureTemplateType loader = (manager, data) -> {
            if (data.getString("Template").isEmpty()) {
                data = data.copy();
                data.putString("Template", legacyTemplate.apply(data).toString());
            }
            return type.load(manager, data);
        };
        // These IDs were used by development builds of the port.
        register((manager, data) -> loader.load(manager, data), alias);
        return register(loader, name);
    }

    private static RegistryObject<StructurePieceType> register(StructurePieceType.StructureTemplateType type, String name) {
        return STRUCTURE_PIECES_DEFERRED.register(name, () -> type);
    }
}
