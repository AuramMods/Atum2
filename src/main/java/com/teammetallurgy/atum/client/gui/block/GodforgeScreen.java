package com.teammetallurgy.atum.client.gui.block;

import net.minecraft.client.gui.GuiGraphics;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.inventory.container.block.GodforgeContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class GodforgeScreen extends AbstractContainerScreen<GodforgeContainer> {
    private static final ResourceLocation GODFORGE_GUI = new ResourceLocation(Atum.MOD_ID, "textures/gui/godforge.png");

    public GodforgeScreen(GodforgeContainer godforge, Inventory playerInventory, Component title) {
        super(godforge, playerInventory, title);
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int width = (this.width - this.imageWidth) / 2;
        int height = (this.height - this.imageHeight) / 2;
        graphics.blit(GODFORGE_GUI, width, height, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isBurning()) {
            int burnLeft = this.menu.getBurnLeftScaled();
            graphics.blit(GODFORGE_GUI, width + 57, height + 49 - burnLeft, 176, 13 - burnLeft, 14, burnLeft + 1);
        }

        int l = this.menu.getCookProgressionScaled();
        graphics.blit(GODFORGE_GUI, width + 80, height + 35, 176, 14, l + 1, 24);

        if (this.menu.fuelSlot.hasItem()) {
            this.renderEmptySlot(graphics, width + 55, height + 52);
        }
    }

    private void renderEmptySlot(@Nonnull GuiGraphics graphics, int x, int y) {
        graphics.blit(GODFORGE_GUI, x, y, 7, 83, 18, 18);
    }
}
