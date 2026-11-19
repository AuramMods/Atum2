package com.teammetallurgy.atum.integration.theoneprobe;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.blocks.base.DoorAtumBlock;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;


import static mcjty.theoneprobe.api.IProbeConfig.ConfigMode.EXTENDED;
import static mcjty.theoneprobe.api.IProbeConfig.ConfigMode.NORMAL;
import static mcjty.theoneprobe.api.TextStyleClass.MODNAME;

public class AtumProbeInfoProvider implements IBlockDisplayOverride {

    @Override
    public boolean overrideStandardInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, BlockState blockState, IProbeHitData data) {
        IProbeConfig config = TOPSupport.getProbeConfig();

        if (mode != ProbeMode.DEBUG && !this.show(mode, config.getShowSilverfish())) {
            if (blockState.getBlock() instanceof DoorAtumBlock door) {
                ItemStack disguise = door.getDisguise();
                if (!disguise.isEmpty()) {
                    probeInfo.horizontal()
                            .item(disguise)
                            .vertical()
                            .itemLabel(disguise)
                            .text(Component.literal(MODNAME + StringUtils.capitalize(Atum.MOD_ID)));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean show(ProbeMode mode, IProbeConfig.ConfigMode cfg) {
        return cfg == NORMAL || (cfg == EXTENDED && mode == ProbeMode.EXTENDED);
    }
}
