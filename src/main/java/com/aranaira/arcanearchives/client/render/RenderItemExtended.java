package com.aranaira.arcanearchives.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderItemExtended {
  private static RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

  // TODO: Are these really needed any more?
  public static void setZLevel(float z) {
    itemRender.zLevel = z;
  }

  public static float getZLevel() {
    return itemRender.zLevel;
  }

  public static void modifyZLevel(float amount) {
    itemRender.zLevel += amount;
  }

  public static void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
    if (!stack.isEmpty()) {
      if (stack.getItem().showDurabilityBar(stack)) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        double health = stack.getItem().getDurabilityForDisplay(stack);
        int rgbfordisplay = stack.getItem().getRGBDurabilityForDisplay(stack);
        int i = Math.round(13.0F - (float) health * 13.0F);
        draw(vertexbuffer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
        draw(vertexbuffer, xPosition + 2, yPosition + 13, i, 1, rgbfordisplay >> 16 & 255, rgbfordisplay >> 8 & 255, rgbfordisplay & 255, 255);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }

      if (stack.getCount() != 1 || text != null) {
        String s = text == null ? String.valueOf(stack.getCount()) : text;
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        float scale = 0.75f;
        GlStateManager.scale(scale, scale, 1.0F);
        fr.drawStringWithShadow(s, (xPosition + 19 - 2 - (fr.getStringWidth(s) * scale)) / scale, (yPosition + 6 + 3 + (1 / (scale * scale) - 1)) / scale, 16777215);
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        // TODO: check if enabled blending still screws things up down
        GlStateManager.enableBlend();
      }

      EntityPlayerSP entityplayersp = Minecraft.getMinecraft().player;
      float f3 = entityplayersp == null ? 0.0F : entityplayersp.getCooldownTracker().getCooldown(stack.getItem(), Minecraft.getMinecraft().getRenderPartialTicks());

      if (f3 > 0.0F) {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        Tessellator tessellator1 = Tessellator.getInstance();
        BufferBuilder vertexbuffer1 = tessellator1.getBuffer();
        draw(vertexbuffer1, xPosition, yPosition + MathHelper.floor(16.0F * (1.0F - f3)), 16, MathHelper.ceil(16.0F * f3), 255, 255, 255, 127);
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
    }
  }

  private static void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    renderer.pos((double) (x), (double) (y), 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos((double) (x), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos((double) (x + width), (double) (y), 0.0D).color(red, green, blue, alpha).endVertex();
    Tessellator.getInstance().draw();
  }

}