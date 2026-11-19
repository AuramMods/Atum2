package com.teammetallurgy.atum.blocks.base;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import javax.annotation.Nullable;

public class DoorAtumBlock extends DoorBlock {
    @Nullable
    private final Block disguise;

    public DoorAtumBlock(Properties properties, BlockSetType blockSetType) {
        this(properties, blockSetType, null);
    }

    public DoorAtumBlock(Block disguise, BlockSetType blockSetType) {
        this(Properties.copy(disguise), blockSetType, disguise);
    }

    private DoorAtumBlock(Properties properties, BlockSetType blockSetType, @Nullable Block disguise) {
        super(properties, blockSetType);
        this.disguise = disguise;
    }

    public ItemStack getDisguise() {
        return this.disguise == null ? ItemStack.EMPTY : new ItemStack(this.disguise);
    }
}