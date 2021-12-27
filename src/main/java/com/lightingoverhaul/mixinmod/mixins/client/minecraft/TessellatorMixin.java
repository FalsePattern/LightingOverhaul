package com.lightingoverhaul.mixinmod.mixins.client.minecraft;

import java.nio.*;

import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import com.lightingoverhaul.mixinmod.helper.ResourceHelper;
import com.lightingoverhaul.mixinmod.helper.shader.RGBShader;
import com.lightingoverhaul.mixinmod.helper.shader.common.Shader;
import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;

@Mixin(Tessellator.class)
@SideOnly(Side.CLIENT)
public abstract class TessellatorMixin implements ITessellatorMixin {
    @Getter
    private RGBShader shader;

    private boolean programInUse;
    private boolean hasFlaggedOpenglError = false;
    private boolean lockedBrightness;
    private float gamma;
    private float sunlevel;
    private float nightVisionWeight;

    private final IntBuffer cachedLightCoord = ByteBuffer.allocateDirect(16).asIntBuffer();
    private final IntBuffer cachedLightCoordSun = ByteBuffer.allocateDirect(16).asIntBuffer();

    public void setupShaders() {
        String vertSource = ResourceHelper.readResourceAsString("/shaders/lightOverlay.vert");
        String fragSource = ResourceHelper.readResourceAsString("/shaders/lightOverlay.frag");

        shader = Shader.builder().addVertex(vertSource).addFragment(fragSource).build(RGBShader::new);
    }

    public void enableShader() {
        shader.bind();
        programInUse = true;
        shader.textureUniform.set(OpenGlHelper.defaultTexUnit - GL13.GL_TEXTURE0);
        shader.gammaUniform.set(gamma);
        shader.sunLevelUniform.set(sunlevel);
        shader.nightVisionWeightUniform.set(nightVisionWeight);
        shader.enableTextureUniform.set(1);
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
        shader.lightCoordUniform.get(cachedLightCoord);
        shader.lightCoordUniform.set(0, 0, 0, 0);
        shader.lightCoordSunUniform.get(cachedLightCoordSun);
        shader.lightCoordSunUniform.set(0, 0, 0, 0);
        GL20.glVertexAttribPointer(shader.lightCoordAttrib, 4, true, false, 32, buffer);
        GL20.glEnableVertexAttribArray(shader.lightCoordAttrib);
    }

    public void unsetLightCoord() {
        GL20.glDisableVertexAttribArray(shader.lightCoordAttrib);
        shader.lightCoordUniform.set(cachedLightCoord);
        shader.lightCoordSunUniform.set(cachedLightCoordSun);
    }

    public int makeBrightness(int lightLevel) {
        return lightLevel << LightingApi._bitshift_l2 | lightLevel << LightingApi._bitshift_r2 | lightLevel << LightingApi._bitshift_g2 | lightLevel << LightingApi._bitshift_b2;
    }

    public boolean isProgramInUse() {
        return programInUse;
    }

    public void setLockedBrightness(boolean locked) {
        lockedBrightness = locked;
    }

    public void enableTexture() {
        shader.enableTextureUniform.set(1);
    }

    public void disableTexture() {
        shader.enableTextureUniform.set(0);
    }

    public void setTextureCoords(float x, float y) {
        GL20.glVertexAttrib2f(shader.texCoordAttrib, x, y);
        GL11.glTexCoord2f(x, y);
    }

    @Shadow
    private static ByteBuffer byteBuffer;

    @Shadow
    private int brightness;

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
        this.brightness = value < 256 ? makeBrightness(value) : value;
    }

    @Inject(method = "<init>*",
            at = @At("RETURN"))
    private void init(CallbackInfo callback) {
        setupShaders();
    }

    @Redirect(method = "addVertex",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/client/renderer/Tessellator;brightness:I",
                       ordinal = 0),
              require = 1)
    private int hackVertexBrightness(Tessellator instance) {
        val brightness = this.brightness;

        int block_r = (brightness >>> LightingApi._bitshift_r2) & 0xF;
        int block_g = (brightness >>> LightingApi._bitshift_g2) & 0xF;
        int block_b = (brightness >>> LightingApi._bitshift_b2) & 0xF;
        int sun_r = (brightness >>> LightingApi._bitshift_sun_r2) & LightingApi._bitmask_sun;
        int sun_g = (brightness >>> LightingApi._bitshift_sun_g2) & LightingApi._bitmask_sun;
        int sun_b = (brightness >>> LightingApi._bitshift_sun_b2) & LightingApi._bitmask_sun;

        return block_r | (block_g << 4) | (block_b << 8) | (sun_r << 16) | (sun_g << 20) | (sun_b << 24);
    }

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Ljava/nio/FloatBuffer;position(I)Ljava/nio/Buffer;",
                       ordinal = 0),
              require = 1)
    private Buffer draw_0(FloatBuffer instance, int i) {
        val ret = instance.position(i);
        setTextureCoord(instance);
        return ret;
    }

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Ljava/nio/ShortBuffer;position(I)Ljava/nio/Buffer;",
                       ordinal = 0),
              require = 1)
    private Buffer draw_1(ShortBuffer instance, int i) {
        val ret = instance.position(i);
        byteBuffer.position(i * 2);
        setLightCoord(byteBuffer);
        return ret;
    }

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Lorg/lwjgl/opengl/GL11;glDisableClientState(I)V",
                       remap = false,
                       ordinal = 1),
              require = 1)
    private void draw_2(int cap) {
        GL11.glDisableClientState(cap);
        unsetTextureCoord();
    }

    @Redirect(method = "draw",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/renderer/OpenGlHelper;setClientActiveTexture(I)V",
                       ordinal = 2),
              require = 1)
    private void draw_3(int p_77472_0_) {
        unsetLightCoord();
        OpenGlHelper.setClientActiveTexture(p_77472_0_);
    }
}
