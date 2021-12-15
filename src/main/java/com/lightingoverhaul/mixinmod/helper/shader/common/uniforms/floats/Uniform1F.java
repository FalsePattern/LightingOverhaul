package com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform1F extends UniformF {
    public Uniform1F(int program, int location) {
        super(program, location);
    }

    public void set(FloatBuffer input) {
        GL20.glUniform1(location, input);
    }

    public void set(float x) {
        GL20.glUniform1f(location, x);
    }
}
