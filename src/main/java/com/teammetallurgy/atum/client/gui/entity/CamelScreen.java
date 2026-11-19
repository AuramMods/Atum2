package com.teammetallurgy.atum.client.gui.entity;

import net.minecraft.client.gui.GuiGraphics;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.inventory.container.entity.CamelContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class CamelScreen extends AbstractContainerScreen<CamelContainer> {
    private static final ResourceLocation CAMEL_GUI_TEXTURE = new ResourceLocation(Atum.MOD_ID, "textures/gui/camel.png");
    private final Inventory playerInventory;
    private float mousePosX;
    private float mousePosY;

    public CamelScreen(CamelContainer container, Inventory playerInv, Component title) {
        super(container, playerInv, title);
        this.playerInventory = playerInv;
        this.imageHeight = 236;
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventory.getDisplayName(), 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int width = (this.width - this.imageWidth) / 2;
        int height = (this.height - this.imageHeight) / 2;
        graphics.blit(CAMEL_GUI_TEXTURE, width, height, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.camel != null) {
            if (this.menu.camel.hasLeftCrate()) {
                graphics.blit(CAMEL_GUI_TEXTURE, width + 7, height + 85, this.imageWidth, 0, this.menu.camel.getInventoryColumns() * 18, 54); //Left Crate
            }
            if (this.menu.camel.hasRightCrate()) {
                graphics.blit(CAMEL_GUI_TEXTURE, width + 97, height + 85, this.imageWidth, 0, this.menu.camel.getInventoryColumns() * 18, 54); //Right Crate
            }
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, width + 88, height + 50, 17, (float) (width + 51) - this.mousePosX, (float) (height + 75 - 50) - this.mousePosY, this.menu.camel);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.mousePosX = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
