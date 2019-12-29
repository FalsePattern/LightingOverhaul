package com.darkshadow44.lightoverhaul.mixins;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

@Mixin(FontRenderer.class)
public abstract class FontRendererMixin {

    @Shadow
    private byte[] glyphWidth;

    @Shadow
    private ResourceLocation locationFontTexture;

    @Shadow
    private TextureManager renderEngine;

    @Shadow
    private float posX;

    @Shadow
    private float posY;

    @Shadow
    private void loadGlyphTexture(int paramInt) {
    }

    @Shadow
    private int[] charWidth;

    private static boolean optifineUpInThisFontRenderer = false;

    @Shadow(remap = false)
    protected abstract void bindTexture(ResourceLocation location);

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public float renderDefaultChar(int c, boolean italics) {
        float tx = (float) (c % 16 * 8);
        float ty = (float) (c / 16 * 8);
        float slant = italics ? 1.0F : 0.0F;

        // Assuming that if the RenderEngine is null, we are in the loading screen
        if (this.renderEngine == null) {
            bindTexture(this.locationFontTexture);
        } else {
            this.renderEngine.bindTexture(this.locationFontTexture); // Avoid using previous method for compatibility
        }

        float width;
        if (optifineUpInThisFontRenderer) {
            width = /* TODO instance.d[c] - 0.01F */ 0;
        } else {
            width = (float) this.charWidth[c] - 0.01F;
        }
        Tessellator tessellator = Tessellator.instance;

        // Safe check to avoid exception during LoadingScreen <=> Main Menu transition
        if (!tessellator.isDrawing) {
            tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
            tessellator.addVertexWithUV(this.posX + slant, this.posY, 0.0F, tx / 128.0F, ty / 128.0F);
            tessellator.addVertexWithUV(this.posX - slant, this.posY + 7.99F, 0.0F, tx / 128.0F, (ty + 7.99F) / 128.0F);
            tessellator.addVertexWithUV(this.posX + width - 1.0F + slant, this.posY, 0.0F, (tx + width - 1.0F) / 128.0F, ty / 128.0F);
            tessellator.addVertexWithUV(this.posX + width - 1.0F - slant, this.posY + 7.99F, 0.0F, (tx + width - 1.0F) / 128.0F, (ty + 7.99F) / 128.0F);
            tessellator.draw();
        }

        return optifineUpInThisFontRenderer ? /* //TODO instance.d[c] */ 0 : (float) this.charWidth[c];
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public float renderUnicodeChar(char c, boolean flag) {
        if (this.glyphWidth[c] == 0) {
            return 0.0F;
        } else {
            int i = c / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[c] >>> 4;
            int k = this.glyphWidth[c] & 15;
            float f = (float) j;
            float f1 = (float) (k + 1);
            float f2 = (float) (c % 16 * 16) + f;
            float f3 = (float) ((c & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = flag ? 1.0F : 0.0F;
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
            tessellator.addVertexWithUV(this.posX + f5, this.posY, 0.0F, f2 / 256.0F, f3 / 256.0F);
            tessellator.addVertexWithUV(this.posX - f5, this.posY + 7.99F, 0.0F, f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            tessellator.addVertexWithUV(this.posX + f4 / 2.0F + f5, this.posY, 0.0F, (f2 + f4) / 256.0F, f3 / 256.0F);
            tessellator.addVertexWithUV(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F, (f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            tessellator.draw();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }
}
