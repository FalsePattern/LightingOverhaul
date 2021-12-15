package com.lightingoverhaul.mixinmod.helper.shader;

import com.lightingoverhaul.coremod.util.RGB;
import com.lightingoverhaul.mixinmod.helper.shader.common.Shader;
import com.lightingoverhaul.mixinmod.helper.shader.common.ShaderSource;
import lombok.Getter;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class RGBShader extends Shader implements IRGBShader{
    @Getter
    public final int texCoordAttrib = getAttribLocation("TexCoord");;
    @Getter
    public final int lightCoordAttrib = getAttribLocation("LightCoord");

    @Getter
    public final int lightCoordUniform = getUniformLocation("u_LightCoord");
    @Getter
    public final int lightCoordSunUniform = getUniformLocation("u_LightCoordSun");
    @Getter
    public final int gammaUniform = getUniformLocation("gamma");
    @Getter
    public final int sunLevelUniform = getUniformLocation("sunlevel");
    @Getter
    public final int nightVisionWeightUniform = getUniformLocation("nightVisionWeight");
    @Getter
    public final int enableTextureUniform = getUniformLocation("enableTexture");

    public int getTextureUniform() {
        return GL20.glGetUniformLocation(program, "Texture");
    }

    private final IntBuffer cachedLightCoord = ByteBuffer.allocateDirect(16).asIntBuffer();
    private final IntBuffer cachedLightCoordSun = ByteBuffer.allocateDirect(16).asIntBuffer();

    public RGBShader(ShaderSource... shaders) {
        super(shaders);
    }

    public void backupLightCoordUniforms() {
        GL20.glGetUniform(program, lightCoordUniform, cachedLightCoord);
        GL20.glUniform4i(lightCoordUniform, 0, 0, 0, 0);
        GL20.glGetUniform(program, lightCoordSunUniform, cachedLightCoordSun);
        GL20.glUniform4i(lightCoordSunUniform, 0, 0, 0, 0);
    }

    public void restoreLightCoordUniforms() {
        GL20.glUniform4(lightCoordUniform, cachedLightCoord);
        GL20.glUniform4(lightCoordSunUniform, cachedLightCoordSun);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends Shader.Builder {
        @Override
        public RGBShader build() {
            return new RGBShader(sources.toArray(new ShaderSource[0]));
        }

        @Override
        public RGBShader.Builder addVertex(String source) {
            super.addVertex(source);
            return this;
        }

        @Override
        public RGBShader.Builder addGeometry(String source) {
            super.addGeometry(source);
            return this;
        }

        @Override
        public RGBShader.Builder addFragment(String source) {
            super.addFragment(source);
            return this;
        }
    }
}
