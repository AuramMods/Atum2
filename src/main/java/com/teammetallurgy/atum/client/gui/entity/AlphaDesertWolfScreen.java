package com.teammetallurgy.atum.client.gui.entity;

import net.minecraft.client.gui.GuiGraphics;
import com.teammetallurgy.atum.Atum;
import com.teammetallurgy.atum.inventory.container.entity.AlphaDesertWolfContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class AlphaDesertWolfScreen extends AbstractContainerScreen<AlphaDesertWolfContainer> {
    private static final ResourceLocation CAMEL_GUI_TEXTURE = new ResourceLocation(Atum.MOD_ID, "textures/gui/desert_wolf.png");
    private float mousePosx;
    private float mousePosY;

    public AlphaDesertWolfScreen(AlphaDesertWolfContainer container, Inventory playerInv, Component title) {
        super(container, playerInv, title);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int width = (this.width - this.imageWidth) / 2;
        int height = (this.height - this.imageHeight) / 2;
        graphics.blit(CAMEL_GUI_TEXTURE, width, height, 0, 0, this.imageWidth, this.imageHeight);

        if (this.menu.desertWolf != null) {
            if (this.menu.desertWolf.isAlpha()) {
                graphics.blit(CAMEL_GUI_TEXTURE, width + 7, height + 35 - 18, 18, this.imageHeight, 18, 18); //Saddle
            }
            graphics.blit(CAMEL_GUI_TEXTURE, width + 7, height + 35, 0, this.imageHeight, 18, 18); //Armor
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, width + 51, height + 60, 17, (float) (width + 51) - this.mousePosx, (float) (height + 75 - 50) - this.mousePosY, this.menu.desertWolf);
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.mousePosx = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
