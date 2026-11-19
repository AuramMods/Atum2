package com.teammetallurgy.atum.misc;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.world.DimensionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.AbstractMap;
import java.util.Optional;

public class SpawnHelper {
    public static final String TAG_ATUM_RESPAWN = "atum_respawn";
    private static final Component BED_MISSING_MSG = Component.literal("You have no home bed or respawn anchor, or it was obstructed");

    /**
     * Gets a player's spawnpoint, resetting if the bed is invalid
     * Always returns a valid level and position which can be teleported to.
     *
     * @param serverLevel any level
     * @param serverPlayer the player
     * @param msgMode how to tell the player if their bed is missing; 0 is no message, 1 is after respawn, 2 is immediately
     * @return surface pos of the spawnpoint
     */
    public static AbstractMap.SimpleEntry<ServerLevel, BlockPos> validateAndGetSpawnPoint(ServerLevel serverLevel, ServerPlayer serverPlayer, int msgMode) {
        boolean atumSpawn = AtumConfig.ATUM_START.startInAtum.get();
        boolean forcedSpawn = serverPlayer.isRespawnForced();
        ServerLevel spawnLevel = serverLevel.getServer().getLevel(serverPlayer.getRespawnDimension());
        BlockPos spawnPos = serverPlayer.getRespawnPosition();

        Optional<Vec3> bedPos = Optional.empty();
        if (spawnPos != null && spawnLevel != null) {
            bedPos = Player.findRespawnPositionAndUseSpawnBlock(spawnLevel, spawnPos, serverPlayer.getRespawnAngle(), forcedSpawn, false);
        }

        boolean reset = false;
        if (atumSpawn) {
            ServerLevel atumLevel = serverLevel.getServer().getLevel(Atum.ATUM);
            BlockPos defaultSpawnPos = getDefaultAtumSpawn(atumLevel);
            BlockPos sharedSpawn = atumLevel.getSharedSpawnPos();
            // best attempt at checking whether this is the default atum spawnpoint
            boolean isSpawnPosDefault = forcedSpawn &&
                    spawnLevel == atumLevel &&
                    spawnPos != null &&
                    (sameColumn(spawnPos, defaultSpawnPos) || sameColumn(spawnPos, sharedSpawn));

            if (bedPos.isEmpty() || isSpawnPosDefault) {
                if (!forcedSpawn && spawnPos != null) {
                    sendBedMissingMsg(serverPlayer, msgMode);
                }
                serverPlayer.setRespawnPosition(Atum.ATUM, defaultSpawnPos, serverPlayer.getYHeadRot(), true, false);
                spawnLevel = atumLevel;
                spawnPos = defaultSpawnPos;
                reset = true;
            }
        } else {
            if (bedPos.isEmpty()) {
                if (!forcedSpawn && spawnPos != null) {
                    sendBedMissingMsg(serverPlayer, msgMode);
                }
                serverPlayer.setRespawnPosition(Level.OVERWORLD, null, serverPlayer.getYHeadRot(), false, false);
                spawnLevel = serverLevel.getServer().getLevel(Level.OVERWORLD);
                spawnPos = DimensionHelper.getSurfacePos(spawnLevel, spawnLevel.getSharedSpawnPos()).above();
                reset = true;
            }
        }

        if (!reset) {
            spawnPos = spawnPos.above();
        }

        return new AbstractMap.SimpleEntry<>(spawnLevel, spawnPos);
    }

    public static BlockPos getDefaultAtumSpawn(ServerLevel level) {
        BlockPos sharedSpawn = level.getSharedSpawnPos();
        for (BlockPos column : BlockPos.spiralAround(sharedSpawn, 8, Direction.EAST, Direction.SOUTH)) {
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            BlockPos ground = surface.below();
            if (level.getWorldBorder().isWithinBounds(surface)
                    && level.isEmptyBlock(surface) && level.isEmptyBlock(surface.above())
                    && level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)
                    && level.getFluidState(ground).isEmpty()) {
                return surface;
            }
        }
        return DimensionHelper.getSurfacePos(level, sharedSpawn).above();
    }

    private static boolean sameColumn(BlockPos first, BlockPos second) {
        return first.getX() == second.getX() && first.getZ() == second.getZ();
    }

    /**
     * Sends a message to a player saying that their bed is missing
     *
     * @param serverPlayer the player
     * @param msgMode how to tell the player if their bed is missing; 0 is no message, 1 is after respawn, 2 is immediately
     */
    public static void sendBedMissingMsg(ServerPlayer serverPlayer, int msgMode) {
        if (msgMode == 1) {
            CompoundTag tag = serverPlayer.getPersistentData();
            CompoundTag persistedTag = tag.getCompound(Player.PERSISTED_NBT_TAG);
            persistedTag.putBoolean(TAG_ATUM_RESPAWN, true);
            tag.put(Player.PERSISTED_NBT_TAG, persistedTag);
        } else if (msgMode == 2) {
            serverPlayer.sendSystemMessage(BED_MISSING_MSG);
        }
    }
}
