package com.teammetallurgy.atum.world.teleporter;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.init.AtumBlocks;
import com.teammetallurgy.atum.init.AtumPoiTypes;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class TeleporterAtum implements ITeleporter {
    public static final TeleporterAtum INSTANCE = new TeleporterAtum();

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }

    public boolean placeInPortal(ServerLevel level, Entity entity, float yaw) {
        PortalInfo portal = this.getPortalInfo(entity, level, null);
        if (portal == null) {
            return false;
        }
        entity.setDeltaMovement(portal.speed);
        entity.moveTo(portal.pos.x, portal.pos.y, portal.pos.z, yaw, portal.xRot);
        return true;
    }

    @Override
    @Nullable
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return getPortalInfoAt(destWorld, entity.blockPosition(), entity.getYRot(), entity.getXRot());
    }

    @Nullable
    PortalInfo getPortalInfoAt(ServerLevel level, BlockPos target, float yaw, float pitch) {
        return teleporterResult(level, target).map(portal -> {
            BlockPos corner = portal.minCorner;
            Vec3 landing = new Vec3(corner.getX() + portal.axis1Size / 2.0,
                    corner.getY() + 1.0, corner.getZ() + portal.axis2Size / 2.0);
            return new PortalInfo(landing, Vec3.ZERO, yaw, pitch);
        }).orElse(null);
    }

    protected Optional<BlockUtil.FoundRectangle> teleporterResult(ServerLevel level, BlockPos pos) {
        return getExistingPortal(level, pos).or(() -> createPortal(level, pos));
    }

    public Optional<BlockUtil.FoundRectangle> getExistingPortal(ServerLevel serverLevel, BlockPos pos) {
        PoiManager posManager = serverLevel.getPoiManager();
        int i = 128;
        posManager.ensureLoadedAndValid(serverLevel, pos, i);
        Optional<PoiRecord> optional = posManager.getInSquare((poiType) -> {
            return poiType.is(AtumPoiTypes.PORTAL.getKey());
        }, pos, i, PoiManager.Occupancy.ANY).sorted(Comparator.<PoiRecord>comparingDouble((poi) -> {
            return poi.getPos().distSqr(pos);
        }).thenComparingInt((poi) -> {
            return poi.getPos().getY();
        })).findFirst();
        return optional.map((poi) -> {
            BlockPos posPos = poi.getPos();
            serverLevel.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(posPos), 3, posPos);
            BlockState blockstate = serverLevel.getBlockState(posPos);
            return BlockUtil.getLargestRectangleAround(posPos, Direction.Axis.X, 9, Direction.Axis.Z, 9, (posIn) -> serverLevel.getBlockState(posIn) == blockstate);
        });
    }

    public Optional<BlockUtil.FoundRectangle> makePortal(ServerLevel level, @Nonnull Entity entity) {
        return createPortal(level, new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ())));
    }

    public Optional<BlockUtil.FoundRectangle> createPortal(Level level, BlockPos pos) {
        if (!level.getWorldBorder().isWithinBounds(pos.offset(-2, 0, -2))
                || !level.getWorldBorder().isWithinBounds(pos.offset(2, 0, 2))) {
            return Optional.empty();
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        pos = new BlockPos(pos.getX(), Mth.clamp(top - 1, level.getMinBuildHeight(), level.getMaxBuildHeight() - 4), pos.getZ());
        BlockState portalState = AtumBlocks.PORTAL.get().defaultBlockState();
        BlockState frame = level.dimension().equals(Level.OVERWORLD)
                ? Blocks.SANDSTONE.defaultBlockState() : AtumBlocks.LIMESTONE_BRICK_LARGE.get().defaultBlockState();

        //Bottom layers
        for (BlockPos basePos : BlockPos.MutableBlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 1, 2))) {
            level.setBlock(basePos, frame, Block.UPDATE_CLIENTS);
        }

        //Pillars
        for (int y = 2; y < 4; y++) {
            level.setBlock(pos.offset(-2, y, -2), frame, Block.UPDATE_CLIENTS);
            level.setBlock(pos.offset(2, y, -2), frame, Block.UPDATE_CLIENTS);
            level.setBlock(pos.offset(-2, y, 2), frame, Block.UPDATE_CLIENTS);
            level.setBlock(pos.offset(2, y, 2), frame, Block.UPDATE_CLIENTS);
        }

        //Portal blocks
        for (BlockPos portalPos : BlockPos.MutableBlockPos.betweenClosed(pos.offset(-1, 1, -1), pos.offset(1, 1, 1))) {
            level.setBlock(portalPos, portalState, Block.UPDATE_CLIENTS);
        }

        //Set air above portal blocks
        for (BlockPos airPos : BlockPos.MutableBlockPos.betweenClosed(pos.offset(-2, 2, -1), pos.offset(2, 3, 1))) {
            level.setBlock(airPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return Optional.of(new BlockUtil.FoundRectangle(pos.offset(-1, 1, -1), 3, 3));
    }
}
