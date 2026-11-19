package com.teammetallurgy.atum.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.api.IFogReductionItem;
import com.teammetallurgy.atum.init.AtumBiomes;
import com.teammetallurgy.atum.init.AtumItems;
import com.teammetallurgy.atum.items.artifacts.nuit.NuitsVanishingItem;
import com.teammetallurgy.atum.misc.AtumConfig;
import com.teammetallurgy.atum.world.SandstormHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Atum.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {
    private static final ResourceLocation MUMMY_BLUR_TEXTURE = new ResourceLocation(Atum.MOD_ID, "textures/hud/mummyblur.png");

    @SubscribeEvent
    public static void renderFog(ViewportEvent.RenderFog event) {
        if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || event.getType() != FogType.NONE
                || !AtumConfig.GENERAL.fogEnabled.get()
                || !(event.getCamera().getEntity() instanceof Player player)
                || player.level().dimension() != Atum.ATUM
                || player.hasEffect(MobEffects.BLINDNESS) || player.hasEffect(MobEffects.DARKNESS)) {
            return;
        }

        BlockPos pos = player.blockPosition();
        float density = 0.05F;
        if (pos.getY() <= 60) {
            density += (62 - pos.getY()) * 0.00333F;
        }
        if (player.level().getBiome(pos).is(AtumBiomes.OASIS)) {
            density *= 0.5F;
        }
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof IFogReductionItem protection
                    && protection.getSlotTypes().contains(Mob.getEquipmentSlotForItem(armor))) {
                density = protection.getFogReduction(density, armor);
            }
        }
        if (pos.getY() >= 56) {
            float storm = Mth.clamp(SandstormHandler.INSTANCE.stormStrength, 0.0F, 1.0F);
            density *= 1.0F + AtumConfig.SANDSTORM.sandstormFog.get() * storm;
        }

        // The old renderer used exponential falloff. Match its visible range in linear fog.
        float far = Math.min(event.getFarPlaneDistance(), 3.0F / Math.max(0.001F, density));
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), far * 0.15F));
        event.setFarPlaneDistance(far);
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event) {
        Optional<ImmutableTriple<String, Integer, ItemStack>> optional = CuriosApi.getCuriosHelper().findEquippedCurio(AtumItems.NUITS_VANISHING.get(), event.getEntity());
        if (optional.isPresent()) {
            if (!NuitsVanishingItem.TIMER.containsKey(event.getEntity()) && !NuitsVanishingItem.isLivingEntityMoving(event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void renderMummyHelmet(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (event.getOverlay() == VanillaGuiOverlay.HELMET.type() && player != null
                && minecraft.options.getCameraType().isFirstPerson()
                && !minecraft.options.hideGui
                && player.getItemBySlot(EquipmentSlot.HEAD).is(AtumItems.MUMMY_HELMET.get())) {
            var graphics = event.getGuiGraphics();
            int width = graphics.guiWidth();
            int height = graphics.guiHeight();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.blit(MUMMY_BLUR_TEXTURE, 0, 0, width, height, 0, 0, 256, 256, 256, 256);
            RenderSystem.disableBlend();
        }
    }
}
