package com.teammetallurgy.atum.integration.jade;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.blocks.base.DoorAtumBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IWailaConfig;

@WailaPlugin(Atum.MOD_ID)
public class JadeIntegration implements IWailaPlugin {
    private static final ResourceLocation HIDE_LIMESTONE_DOORS = new ResourceLocation(Atum.MOD_ID, "hide_limestone_doors");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(HIDE_LIMESTONE_DOORS, true);
        registration.addRayTraceCallback((hit, accessor, original) -> {
            if (accessor instanceof BlockAccessor block && block.getBlock() instanceof DoorAtumBlock door
                    && IWailaConfig.get().getPlugin().get(HIDE_LIMESTONE_DOORS)) {
                ItemStack disguise = door.getDisguise();
                if (!disguise.isEmpty()) {
                    return registration.blockAccessor().from(block).fakeBlock(disguise).build();
                }
            }
            return accessor;
        });
    }
}
