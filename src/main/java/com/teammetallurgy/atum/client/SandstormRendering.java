package com.teammetallurgy.atum.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.init.AtumBiomes;
import com.teammetallurgy.atum.items.artifacts.ArtifactArmor;
import com.teammetallurgy.atum.misc.AtumConfig;
import com.teammetallurgy.atum.world.SandstormHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = Atum.MOD_ID, value = Dist.CLIENT)
public final class SandstormRendering {
    private static final ResourceLocation SAND_BLUR_TEXTURE = new ResourceLocation(Atum.MOD_ID, "textures/hud/sandstormwip.png");
    private static float previousIntensity;
    private static float intensity;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.level().dimension() != Atum.ATUM || !AtumConfig.SANDSTORM.sandstormEnabled.get()) {
            previousIntensity = intensity = 0.0F;
            return;
        }

        previousIntensity = intensity;
        BlockPos pos = player.blockPosition();
        boolean exposed = pos.getY() >= 50 && player.level().canSeeSkyFromBelowWater(pos)
                && !player.level().getBiome(pos).is(AtumBiomes.OASIS);
        float target = exposed ? Mth.clamp(SandstormHandler.INSTANCE.stormStrength, 0.0F, 1.0F) : 0.0F;
        intensity = Mth.approach(intensity, target, exposed ? 0.04F : 0.025F);
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        float strength = Mth.lerp(partialTick, previousIntensity, intensity);
        if (strength <= 0.0F || player == null || player.level().dimension() != Atum.ATUM) {
            return;
        }

        float brightness = AtumConfig.SANDSTORM.sandDarkness.get() / 100.0F;
        brightness *= 1.0F - player.level().getSkyDarken() / 15.0F;
        float baseTransparency = AtumConfig.SANDSTORM.sandAlpha.get() / 100.0F;
        float armorTransparency = player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArtifactArmor
                ? AtumConfig.SANDSTORM.sandEyesAlpha.get() / 100.0F : 1.0F;
        float time = (player.level().getGameTime() % 24000L) + partialTick;
        Matrix4f pose = graphics.pose().last().pose();

        graphics.flush();
        gui.setupOverlayRenderState(true, false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, SAND_BLUR_TEXTURE);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        for (int layer = 1; layer <= 6; layer++) {
            float alpha = (float) Math.pow(Math.max(0.0F, strength - baseTransparency), layer)
                    * strength * armorTransparency;
            float scale = 0.2F / layer;
            float u = -time / (25.0F - layer * 0.75F) + player.getYRot() / (1440.0F * scale);
            float v = player.getXRot() / (720.0F * scale);
            float uMax = u + width / (480.0F * scale);
            float vMax = v + height / (720.0F * scale);

            buffer.vertex(pose, 0, height, 0).uv(u, vMax).color(brightness, brightness, brightness, alpha).endVertex();
            buffer.vertex(pose, width, height, 0).uv(uMax, vMax).color(brightness, brightness, brightness, alpha).endVertex();
            buffer.vertex(pose, width, 0, 0).uv(uMax, v).color(brightness, brightness, brightness, alpha).endVertex();
            buffer.vertex(pose, 0, 0, 0).uv(u, v).color(brightness, brightness, brightness, alpha).endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.disableBlend();
    }
}
