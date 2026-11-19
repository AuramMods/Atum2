package com.teammetallurgy.atum.blocks.vegetation;

import com.teammetallurgy.atum.init.AtumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nonnull;

public class DryGrassBlock extends OasisGrassBlock {

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        return ground.is(AtumBlocks.LIMESTONE_GRAVEL.get()) || super.mayPlaceOn(ground, level, pos);
    }

    @Override
    @Nonnull
    public PlantType getPlantType(BlockGetter level, BlockPos pos) {
        return PlantType.DESERT;
    }
}
