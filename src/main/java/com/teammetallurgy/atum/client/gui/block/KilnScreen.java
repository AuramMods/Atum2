package com.teammetallurgy.atum.client.gui.block;

import net.minecraft.client.gui.GuiGraphics;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.inventory.container.block.KilnContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class KilnScreen extends AbstractContainerScreen<KilnContainer> {
    public static final ResourceLocation KILN_GUI = new ResourceLocation(Atum.MOD_ID, "textures/gui/kiln.png");
    private final Inventory playerInventory;

    public KilnScreen(KilnContainer kiln, Inventory playerInv, Component title) {
        super(kiln, playerInv, title);
        this.playerInventory = playerInv;
        this.imageHeight = 192;
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        Component s = this.title;
        graphics.drawString(this.font, s, this.font.width(s) / 2, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventory.getDisplayName(), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int width = (this.width - this.imageWidth) / 2;
        int height = (this.height - this.imageHeight) / 2;
        graphics.blit(KILN_GUI, width, height, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.isBurning()) {
            int burn = this.menu.getBurnLeftScaled();
            graphics.blit(KILN_GUI, width + 37, height + 32 + 12 - burn, 176, 12 - burn, 14, burn + 1);
        }

        int cookProgress = this.menu.getCookProgressionScaled();
        graphics.blit(KILN_GUI, width + 78, height + 52, 176, 14, 19, cookProgress);
    }
}
