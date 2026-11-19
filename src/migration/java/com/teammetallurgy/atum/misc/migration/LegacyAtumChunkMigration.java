package com.teammetallurgy.atum.misc.migration;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class LegacyAtumChunkMigration {
    private static final int BIOMES_PER_SECTION = 64;
    private static final int BIOMES_PER_CHUNK = 1024;
    private final Map<Integer, String> legacyBiomes;

    LegacyAtumChunkMigration(CompoundTag level) {
        this.legacyBiomes = new HashMap<>();
        ListTag ids = level.getCompound("fml").getCompound("Registries")
                .getCompound("minecraft:worldgen/biome").getList("ids", Tag.TAG_COMPOUND);
        for (Tag row : ids) {
            var entry = (CompoundTag) row;
            String key = entry.getString("K");
            if (key.startsWith("atum:")) {
                this.legacyBiomes.put(entry.getInt("V"), key);
            }
        }
    }

    Optional<CompoundTag> upgrade(CompoundTag chunk, int dataVersion) {
        int[] biomes = chunk.getCompound("Level").getIntArray("Biomes");
        if (biomes.length != BIOMES_PER_CHUNK || this.legacyBiomes.isEmpty()) {
            return Optional.empty();
        }
        SharedConstants.tryDetectVersion();
        // Custom dimensions keep their vertical section coordinates during the vanilla chunk upgrade.
        var context = new CompoundTag();
        context.putString("dimension", "atum:atum");
        context.putString("generator", "minecraft:noise");
        chunk.put("__context", context);
        CompoundTag current = DataFixTypes.CHUNK.updateToCurrentVersion(DataFixers.getDataFixer(), chunk, dataVersion);
        NbtUtils.addCurrentDataVersion(current);
        current.remove("__context");

        for (Tag value : current.getList("sections", Tag.TAG_COMPOUND)) {
            var section = (CompoundTag) value;
            int offset = section.getByte("Y") * BIOMES_PER_SECTION;
            if (offset >= 0 && offset < biomes.length && section.contains("biomes", Tag.TAG_COMPOUND)) {
                this.restorePalette(section.getCompound("biomes"), biomes, offset);
            }
        }
        repairSpawners(current.getList("block_entities", Tag.TAG_COMPOUND));
        return Optional.of(current);
    }

    private static void repairSpawners(ListTag blockEntities) {
        for (Tag value : blockEntities) {
            var spawner = (CompoundTag) value;
            if (!spawner.getString("id").equals("minecraft:mob_spawner")) {
                continue;
            }
            CompoundTag spawn = spawner.getCompound("SpawnData");
            if (spawn.contains("id", Tag.TAG_STRING) && !spawn.contains("entity", Tag.TAG_COMPOUND)) {
                var updated = new CompoundTag();
                updated.put("entity", spawn);
                spawner.put("SpawnData", updated);
            }
            ListTag potentials = spawner.getList("SpawnPotentials", Tag.TAG_COMPOUND);
            for (int i = 0; i < potentials.size(); i++) {
                CompoundTag potential = potentials.getCompound(i);
                if (potential.contains("Entity", Tag.TAG_COMPOUND)) {
                    var data = new CompoundTag();
                    data.put("entity", potential.getCompound("Entity"));
                    var updated = new CompoundTag();
                    updated.put("data", data);
                    updated.putInt("weight", potential.getInt("Weight"));
                    potentials.set(i, updated);
                }
            }
        }
    }

    private void restorePalette(CompoundTag biomeData, int[] biomes, int offset) {
        ListTag vanillaPalette = biomeData.getList("palette", Tag.TAG_STRING);
        if (vanillaPalette.isEmpty()) {
            return;
        }
        int vanillaBits = Mth.ceillog2(vanillaPalette.size());
        BitStorage vanillaIndices = vanillaBits == 0 ? new ZeroBitStorage(BIOMES_PER_SECTION)
                : new SimpleBitStorage(vanillaBits, BIOMES_PER_SECTION, biomeData.getLongArray("data"));
        var palette = new ListTag();
        var indicesByName = new HashMap<String, Integer>();
        var indices = new int[BIOMES_PER_SECTION];
        for (int i = 0; i < indices.length; i++) {
            String name = this.legacyBiomes.getOrDefault(biomes[offset + i], vanillaPalette.getString(vanillaIndices.get(i)));
            indices[i] = indicesByName.computeIfAbsent(name, key -> {
                int index = palette.size();
                palette.add(StringTag.valueOf(key));
                return index;
            });
        }
        biomeData.put("palette", palette);
        if (palette.size() == 1) {
            biomeData.remove("data");
        } else {
            var storage = new SimpleBitStorage(Mth.ceillog2(palette.size()), BIOMES_PER_SECTION, indices);
            biomeData.putLongArray("data", storage.getRaw());
        }
    }
}
