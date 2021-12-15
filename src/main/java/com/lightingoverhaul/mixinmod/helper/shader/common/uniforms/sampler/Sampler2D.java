package com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.sampler;

import com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints.Uniform1I;
import org.lwjgl.opengl.GL20;

public class Sampler2D extends Uniform1I {
    public Sampler2D(int program, int location) {
        super(program, location);
    }

    @Override
    public int type() {
        return GL20.GL_SAMPLER_2D;
    }
}
