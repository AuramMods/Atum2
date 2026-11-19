package com.teammetallurgy.atum.world.teleporter;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.init.AtumFeatures;
import com.teammetallurgy.atum.misc.AtumConfig;
import com.teammetallurgy.atum.misc.SpawnHelper;
import com.teammetallurgy.atum.world.DimensionHelper;
import com.teammetallurgy.atum.world.gen.feature.BonusCrateFeature;
import com.teammetallurgy.atum.world.gen.feature.StartStructureFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.util.function.Function;

public class TeleporterAtumStart implements ITeleporter {

    @Override
    @Nullable
    public PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        BlockPos spawn = DimensionHelper.getSurfacePos(destWorld, destWorld.getSharedSpawnPos());
        if (AtumConfig.ATUM_START.startInAtumPortal.get()) {
            PortalInfo portal = TeleporterAtum.INSTANCE.getPortalInfoAt(destWorld, spawn, entity.getYRot(), entity.getXRot());
            if (portal != null) {
                return portal;
            }
        }
        return new PortalInfo(Vec3.atBottomCenterOf(SpawnHelper.getDefaultAtumSpawn(destWorld)), Vec3.ZERO, entity.getYRot(), entity.getXRot());
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        if (destWorld.dimension().equals(Atum.ATUM) && !DimensionHelper.getData(destWorld).hasStartStructureSpawned()) {
            BlockPos spawn = DimensionHelper.getSurfacePos(destWorld, destWorld.getSharedSpawnPos());
            if (AtumConfig.ATUM_START.startInAtumPortal.get()) {
                spawn = DimensionHelper.getSurfacePos(destWorld, spawn.offset(4, 0, 4));
            }
            this.onInitialAtumJoining(destWorld, spawn);
            DimensionHelper.getData(destWorld).setHasStartStructureSpawned(true);
        }
        return repositionEntity.apply(false);
    }

    private void onInitialAtumJoining(ServerLevel level, BlockPos spawnPos) {
        if (!AtumConfig.ATUM_START.atumStartStructure.get().isEmpty()) {
            StartStructureFeature startStructure = AtumFeatures.START_STRUCTURE.get();
            startStructure.place(FeatureConfiguration.NONE, level, level.getChunkSource().getGenerator(), level.random, spawnPos);
        }
        if (level.getServer().getWorldData().worldGenOptions().generateBonusChest()) {
            BonusCrateFeature bonusCrate = AtumFeatures.BONUS_CRATE.get();
            bonusCrate.place(FeatureConfiguration.NONE, level, level.getChunkSource().getGenerator(), level.random, spawnPos);
        }
    }
}
