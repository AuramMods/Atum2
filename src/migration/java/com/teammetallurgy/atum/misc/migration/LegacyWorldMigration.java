package com.teammetallurgy.atum.misc.migration;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

public final class LegacyWorldMigration {
    private static final String DIMENSION_ID = "atum:atum";
    private static final int DATA_VERSION_1_16_5 = 2586;
    private static final int REGION_SIZE = 32;
    private static final Pattern REGION_FILE = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");

    private LegacyWorldMigration() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Pass the 1.16.5 world directory followed by the new world directory.");
        }

        Path source = Path.of(args[0]).toRealPath();
        Path requestedPath = Path.of(args[1]).toAbsolutePath().normalize();
        if (requestedPath.getParent() == null) {
            throw new IllegalArgumentException("Choose a new save directory within an existing directory.");
        }
        Path destination = requestedPath.getParent().toRealPath().resolve(requestedPath.getFileName());
        if (destination.startsWith(source)) {
            throw new IllegalArgumentException("Place the destination outside the source world.");
        }

        Path sessionLock = source.resolve("session.lock");
        if (Files.notExists(sessionLock)) {
            migrateWorld(source, destination);
        } else {
            try (var channel = FileChannel.open(sessionLock, StandardOpenOption.READ);
                 var lock = channel.tryLock(0, Long.MAX_VALUE, true)) {
                if (lock == null) {
                    throw new IOException("Close the Minecraft world before copying it: " + source);
                }
                migrateWorld(source, destination);
            }
        }
    }

    private static void migrateWorld(Path source, Path destination) throws IOException {
        CompoundTag level = NbtIo.readCompressed(source.resolve("level.dat").toFile());
        updateDimension(level);

        // Publish the converted save only after the entire copy has finished.
        Path staging = Files.createTempDirectory(destination.getParent(), ".atum-migration-");
        try {
            copyWorld(source, staging);
            long entities = migrateRegions(staging, new LegacyAtumChunkMigration(level));
            NbtIo.writeCompressed(level, staging.resolve("level.dat").toFile());
            Files.move(staging, destination);
            System.out.printf("Copied %d legacy entities and migrated the Atum dimension to %s%n", entities, destination);
        } catch (IOException | RuntimeException failure) {
            throw new IOException("Migration stopped. The original is intact. Incomplete copy: " + staging, failure);
        }
    }

    private static void updateDimension(CompoundTag level) throws IOException {
        CompoundTag data = level.getCompound("Data");
        CompoundTag dimensions = data.getCompound("WorldGenSettings").getCompound("dimensions");
        CompoundTag atum = dimensions.getCompound(DIMENSION_ID);
        if (data.getInt("DataVersion") > DATA_VERSION_1_16_5
                || !"atum:noise".equals(atum.getCompound("generator").getString("type"))) {
            throw new IllegalArgumentException("The source does not contain the legacy Atum dimension generator.");
        }

        try (InputStream resource = LegacyWorldMigration.class.getResourceAsStream("/data/atum/dimension/atum.json")) {
            if (resource == null) {
                throw new IOException("The Atum dimension data is missing from the runtime resources.");
            }
            var json = JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            CompoundTag current = (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
            atum.put("generator", current.getCompound("generator"));
        }
    }

    private static void copyWorld(Path source, Path staging) throws IOException {
        try (var paths = Files.walk(source, FileVisitOption.FOLLOW_LINKS)) {
            var iterator = paths.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                if (!path.equals(source) && !path.equals(source.resolve("session.lock"))) {
                    if (Files.isSymbolicLink(path) && staging.startsWith(path.toRealPath())) {
                        throw new IOException("A link in the source would copy the destination into itself: " + path);
                    }
                    Files.copy(path, staging.resolve(source.relativize(path)), StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    private static long migrateRegions(Path world, LegacyAtumChunkMigration biomeMigration) throws IOException {
        long total = 0;
        try (var paths = Files.find(world, Integer.MAX_VALUE, (path, attributes) -> attributes.isRegularFile()
                && isDimensionRegion(world.relativize(path)))) {
            for (Path region : paths.toList()) {
                var coordinates = REGION_FILE.matcher(region.getFileName().toString());
                if (coordinates.matches()) {
                    int x = Integer.parseInt(coordinates.group(1)) * REGION_SIZE;
                    int z = Integer.parseInt(coordinates.group(2)) * REGION_SIZE;
                    LegacyAtumChunkMigration atum = region.getParent().equals(world.resolve("dimensions/atum/atum/region"))
                            ? biomeMigration : null;
                    total += migrateRegion(region, x, z, atum);
                }
            }
        }
        return total;
    }

    private static boolean isDimensionRegion(Path path) {
        int segments = path.getNameCount();
        if (segments < 2 || !path.getName(segments - 2).toString().equals("region")) {
            return false;
        }
        return switch (path.getName(0).toString()) {
            case "region" -> segments == 2;
            case "DIM-1", "DIM1" -> segments == 3;
            case "dimensions" -> segments >= 5;
            default -> false;
        };
    }

    private static int migrateRegion(Path source, int originX, int originZ, LegacyAtumChunkMigration biomeMigration) throws IOException {
        int entityCount = 0;
        RegionFile outputRegion = null;
        try (var region = new RegionFile(source, source.getParent(), false)) {
            for (int x = originX; x < originX + REGION_SIZE; x++) {
                for (int z = originZ; z < originZ + REGION_SIZE; z++) {
                    ChunkPos position = new ChunkPos(x, z);
                    if (!region.hasChunk(position)) {
                        continue;
                    }
                    CompoundTag old;
                    try (var input = region.getChunkDataInputStream(position)) {
                        if (input == null) {
                            throw new IOException("Cannot read chunk " + position + " in " + source);
                        }
                        old = NbtIo.read(input);
                    }
                    int dataVersion = old.getInt("DataVersion");
                    if (dataVersion > DATA_VERSION_1_16_5) {
                        continue;
                    }

                    ListTag entities = old.getCompound("Level").getList("Entities", Tag.TAG_COMPOUND);
                    if (!entities.isEmpty()) {
                        if (outputRegion == null) {
                            Path directory = Files.createDirectories(source.getParent().resolveSibling("entities"));
                            outputRegion = new RegionFile(directory.resolve(source.getFileName()), directory, false);
                        }
                        if (outputRegion.hasChunk(position)) {
                            throw new IOException("The source already has an entity record for chunk " + position + " in " + source);
                        }

                        // Keep the original data version so Minecraft can upgrade the entity data on load.
                        CompoundTag chunk = new CompoundTag();
                        chunk.put("Entities", entities);
                        chunk.putIntArray("Position", new int[] {x, z});
                        chunk.putInt("DataVersion", dataVersion);
                        writeChunk(outputRegion, position, chunk);
                        old.getCompound("Level").remove("Entities");
                        entityCount += entities.size();
                    }

                    if (biomeMigration != null) {
                        var upgraded = biomeMigration.upgrade(old, dataVersion);
                        if (upgraded.isPresent()) {
                            writeChunk(region, position, upgraded.get());
                            continue;
                        }
                    }
                    if (!entities.isEmpty()) {
                        writeChunk(region, position, old);
                    }
                }
            }
        } finally {
            if (outputRegion != null) {
                outputRegion.close();
            }
        }
        return entityCount;
    }

    private static void writeChunk(RegionFile region, ChunkPos position, CompoundTag data) throws IOException {
        try (var output = region.getChunkDataOutputStream(position)) {
            NbtIo.write(data, output);
        }
    }
}
