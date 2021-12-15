package com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.Uniform;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public abstract class UniformF extends Uniform {
    public UniformF(int program, int location) {
        super(program, location);
    }

    public abstract void set(FloatBuffer input);

    public void get(FloatBuffer output) {
        GL20.glGetUniform(program, location, output);
    }
}
