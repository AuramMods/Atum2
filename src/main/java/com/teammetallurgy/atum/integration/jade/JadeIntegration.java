package com.teammetallurgy.atum.integration.jade;

import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.blocks.base.DoorAtumBlock;
import com.teammetallurgy.atum.blocks.vegetation.DateBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.Identifiers;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.config.IWailaConfig;

@WailaPlugin(Atum.MOD_ID)
public class JadeIntegration implements IWailaPlugin {
    private static final ResourceLocation HIDE_LIMESTONE_DOORS = new ResourceLocation(Atum.MOD_ID, "hide_limestone_doors");
    private static final ResourceLocation DATE_GROWTH = new ResourceLocation(Atum.MOD_ID, "date_growth");

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
        registration.registerBlockComponent(DateGrowth.INSTANCE, DateBlock.class);
    }

    private enum DateGrowth implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!config.get(Identifiers.MC_CROP_PROGRESS)) {
                return;
            }
            int age = accessor.getBlockState().getValue(DateBlock.AGE);
            Component growth = age == 7 ? Component.translatable("tooltip.jade.crop_mature")
                    : Component.literal(Math.round(age * 100.0F / 7) + "%");
            tooltip.add(Component.translatable("tooltip.jade.crop_growth", growth));
        }

        @Override
        public ResourceLocation getUid() {
            return DATE_GROWTH;
        }
    }
}
