package com.teammetallurgy.atum.blocks.base;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AtumPaneBlock extends IronBarsBlock {

    public AtumPaneBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).instrument(NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).noOcclusion());
    }
}