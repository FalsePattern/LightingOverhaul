package com.lightingoverhaul.mixinmod.mixins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;
import java.util.Arrays;

import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import com.lightingoverhaul.mixinmod.helper.shader.IRGBShader;
import com.lightingoverhaul.mixinmod.helper.shader.RGBShader;
import com.lightingoverhaul.mixinmod.helper.shader.common.ShaderException;
import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements ITessellatorMixin {

    @Shadow
    public boolean hasColor;

    @Shadow
    public int brightness;

    @Shadow
    public boolean hasTexture;

    @Shadow
    public boolean hasBrightness;

    @Shadow
    public boolean hasNormals;

    @Shadow
    public int color;

    @Shadow
    public double xOffset;

    @Shadow
    public double yOffset;

    @Shadow
    public double zOffset;

    @Shadow
    public int normal;

    @Shadow
    public int[] rawBuffer;

    @Shadow
    public int rawBufferIndex;

    @Shadow
    public int addedVertices;

    @Shadow
    public int vertexCount;

    @Shadow
    public double textureU;

    @Shadow
    public double textureV;

    private int rawBufferSize;

    private static RGBShader shader;

    private static boolean programInUse;

    private static boolean hasFlaggedOpenglError;
    private static boolean lockedBrightness;
    private static float gamma;
    private static float sunlevel;
    private static float nightVisionWeight;

    static {
        hasFlaggedOpenglError = false;
    }

    @Inject(method = "setBrightness",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void brightnessLock(CallbackInfo ci) {
        if (lockedBrightness) ci.cancel();
    }

    @Redirect(method = "setBrightness",
                    at = @At(value = "FIELD",
                             target = "Lnet/minecraft/client/renderer/Tessellator;brightness:I",
                             opcode = Opcodes.PUTFIELD),
                    require = 1)
    private void customBrightness(Tessellator instance, int value) {
        instance.brightness = value < 256 ? makeBrightness(value) : value;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void init(CallbackInfo callback) {
        setupShaders();
    }

    public void setupShaders() {
        String vertSource = readResourceAsString("/shaders/lightOverlay.vert");
        String fragSource = readResourceAsString("/shaders/lightOverlay.frag");

        try {
            shader = RGBShader.builder().addVertex(vertSource).addFragment(fragSource).build();
        } catch (ShaderException e) {
            CoreLoadingPlugin.CLLog.error(e.getMessage());
        }
    }

    private String readResourceAsString(String path) {
        InputStream is = ITessellatorMixin.class.getResourceAsStream(path);
        if (is == null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder source = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                source.append(line).append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source.toString();
    }

    @Override
    public IRGBShader getShader() {
        return shader;
    }

    public void enableShader() {
        shader.bind();
        programInUse = true;
        int textureUniform = shader.getTextureUniform();
        GL20.glUniform1i(textureUniform, OpenGlHelper.defaultTexUnit - GL13.GL_TEXTURE0);
        GL20.glUniform1f(shader.gammaUniform, gamma);
        GL20.glUniform1f(shader.sunLevelUniform, sunlevel);
        GL20.glUniform1f(shader.nightVisionWeightUniform, nightVisionWeight);
        GL20.glUniform1i(shader.enableTextureUniform, 1);
    }

    public void disableShader() {
        programInUse = false;
        shader.unbind();
    }

    public void setTextureCoord(FloatBuffer buffer) {
        int lastGLErrorCode;
        if ((lastGLErrorCode = GL11.glGetError()) != GL11.GL_NO_ERROR) {
            if (!hasFlaggedOpenglError) {
                CoreLoadingPlugin.CLLog.warn("Render error entering CLTessellatorHelper.setTextureCoord()! Error Code: " + lastGLErrorCode + ". Trying to proceed anyway...");
                hasFlaggedOpenglError = true;
            }
        }
        GL20.glVertexAttribPointer(shader.texCoordAttrib, 2, false, 32, buffer);
        GL20.glEnableVertexAttribArray(shader.texCoordAttrib);
    }

    public void unsetTextureCoord() {
        GL20.glDisableVertexAttribArray(shader.texCoordAttrib);
    }

    public void updateShaders(float newGamma, float newSunlevel, float newNightVisionWeight) {
        gamma = newGamma;
        sunlevel = newSunlevel;
        nightVisionWeight = newNightVisionWeight;
    }

    public void setLightCoord(ByteBuffer buffer) {
        shader.backupLightCoordUniforms();
        GL20.glVertexAttribPointer(shader.lightCoordAttrib, 4, true, false, 32, buffer);
        GL20.glEnableVertexAttribArray(shader.lightCoordAttrib);
    }

    public void unsetLightCoord() {
        GL20.glDisableVertexAttribArray(shader.lightCoordAttrib);
        shader.restoreLightCoordUniforms();
    }

    public int makeBrightness(int lightLevel) {
        return lightLevel << LightingApi._bitshift_l2 | lightLevel << LightingApi._bitshift_r2 | lightLevel << LightingApi._bitshift_g2 | lightLevel << LightingApi._bitshift_b2;
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public void addVertex(double par1, double par3, double par5) {

        if (this.rawBufferIndex >= rawBufferSize - 32) {
            if (rawBufferSize == 0) {
                rawBufferSize = 0x10000; // 65536
                this.rawBuffer = new int[rawBufferSize];
            } else {
                rawBufferSize *= 2;
                this.rawBuffer = Arrays.copyOf(this.rawBuffer, rawBufferSize);
            }
        }

        ++this.addedVertices;

        if (this.hasTexture) {
            this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float) this.textureU);
            this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float) this.textureV);
        }

        if (this.hasBrightness) {
            /*
             * << and >> take precedence over & Incoming: 0000 0000 SSSS BBBB GGGG RRRR LLLL
             * 0000
             */
            int block_r = (this.brightness >> LightingApi._bitshift_r2) & 0xF;
            int block_g = (this.brightness >> LightingApi._bitshift_g2) & 0xF;
            int block_b = (this.brightness >> LightingApi._bitshift_b2) & 0xF;
            int sun_r = (this.brightness >> LightingApi._bitshift_sun_r2) & LightingApi._bitmask_sun;
            int sun_g = (this.brightness >> LightingApi._bitshift_sun_g2) & LightingApi._bitmask_sun;
            int sun_b = (this.brightness >> LightingApi._bitshift_sun_b2) & LightingApi._bitmask_sun;

            /* 0000 SSSS 0000 BBBB 0000 GGGG 0000 RRRR */
            this.rawBuffer[this.rawBufferIndex + 7] = block_r | (block_g << 4) | (block_b << 8) | (sun_r << 16) | (sun_g << 20) | (sun_b << 24);
        }

        if (this.hasColor) {
            this.rawBuffer[this.rawBufferIndex + 5] = this.color;
        }

        if (this.hasNormals) {
            this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
        }

        this.rawBuffer[this.rawBufferIndex] = Float.floatToRawIntBits((float) (par1 + this.xOffset));
        this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float) (par3 + this.yOffset));
        this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float) (par5 + this.zOffset));
        this.rawBufferIndex += 8;
        ++this.vertexCount;

    }

    public boolean isProgramInUse() {
        return programInUse;
    }

    public void setLockedBrightness(boolean locked) {
        lockedBrightness = locked;
    }

    @Shadow
    private static IntBuffer intBuffer;
    @Shadow
    private static ByteBuffer byteBuffer;
    @Shadow
    private static FloatBuffer floatBuffer;
    @Shadow
    public boolean isDrawing;
    @Shadow
    private static ShortBuffer shortBuffer;

    @Shadow
    public int drawMode;

    @Shadow
    private void reset() {
    }

//    @Redirect(method = "draw",
//            at = @At(value = "INVOKE",
//                     target = "Ljava/nio/FloatBuffer;position(I)Ljava/nio/Buffer;",
//                     ordinal = 0),
//
//            require = 1)
//    private Buffer draw_0(FloatBuffer instance, int i) {
//        val ret = instance.position(i);
//        setTextureCoord(floatBuffer);
//        return ret;
//    }
//
//    @Redirect(method = "draw",
//            at = @At(value = "INVOKE",
//                     target = "Ljava/nio/ShortBuffer;position(I)Ljava/nio/Buffer;",
//                     ordinal = 0),
//            require = 1)
//    private Buffer draw_1(ShortBuffer instance, int i) {
//        val ret = instance.position(i);
//        byteBuffer.position(28);
//        setLightCoord(byteBuffer);
//        return ret;
//    }
//
//    @Redirect(method = "draw",
//            at = @At(value = "INVOKE",
//                     target = "Lorg/lwjgl/opengl/GL11;glDisableClientState(I)V",
//                     shift = At.Shift.AFTER,
//                     ordinal = 1),
//            require = 1)
//    private void draw_2(CallbackInfoReturnable<Integer> cir) {
//        unsetTextureCoord();
//    }
//
//    @Inject(method = "draw",
//            at = @At(value = "INVOKE",
//                     target = "Lnet/minecraft/client/renderer/OpenGlHelper;setClientActiveTexture(I)V",
//                     shift = At.Shift.AFTER,
//                     ordinal = 2),
//            require = 1)
//    private void draw_3(CallbackInfoReturnable<Integer> cir) {
//        unsetLightCoord();
//    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public int draw() {
        if (!this.isDrawing)
            throw new IllegalStateException("Not tesselating!");
        this.isDrawing = false;
        if (this.vertexCount > 0) {
            intBuffer.clear();
            intBuffer.put(this.rawBuffer, 0, this.rawBufferIndex);
            byteBuffer.position(0);
            byteBuffer.limit(this.rawBufferIndex * 4);
            if (this.hasTexture) {
                floatBuffer.position(3);
                /* ADD */ setTextureCoord(floatBuffer);
                GL11.glTexCoordPointer(2, 32, floatBuffer);
                GL11.glEnableClientState(32888);
            }
            if (this.hasBrightness) {
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                shortBuffer.position(14);
                byteBuffer.position(28);
                /* ADD */ setLightCoord(byteBuffer);
                GL11.glTexCoordPointer(2, 32, shortBuffer);
                GL11.glEnableClientState(32888);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            }
            if (this.hasColor) {
                byteBuffer.position(20);
                GL11.glColorPointer(4, true, 32, byteBuffer);
                GL11.glEnableClientState(32886);
            }
            if (this.hasNormals) {
                byteBuffer.position(24);
                GL11.glNormalPointer(32, byteBuffer);
                GL11.glEnableClientState(32885);
            }
            floatBuffer.position(0);
            GL11.glVertexPointer(3, 32, floatBuffer);
            GL11.glEnableClientState(32884);
            GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
            GL11.glDisableClientState(32884);
            if (this.hasTexture) {
                GL11.glDisableClientState(32888);
                /* ADD */unsetTextureCoord();
            }
            if (this.hasBrightness) {
                /* ADD */ this.unsetLightCoord();
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GL11.glDisableClientState(32888);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            }
            if (this.hasColor)
                GL11.glDisableClientState(32886);
            if (this.hasNormals)
                GL11.glDisableClientState(32885);
        }
        int i = this.rawBufferIndex * 4;
        reset();
        return i;
    }

    public void enableTexture() {
        GL20.glUniform1i(shader.enableTextureUniform, 1);
    }

    public void disableTexture() {
        GL20.glUniform1i(shader.enableTextureUniform, 0);
    }

    public void setTextureCoords(float x, float y) {
        GL20.glVertexAttrib2f(shader.texCoordAttrib, x, y);
        GL11.glTexCoord2f(x, y);
    }
}
