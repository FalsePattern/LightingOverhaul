package com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform3F extends UniformF {
    public Uniform3F(int program, int location) {
        super(program, location);
    }

    public void set(FloatBuffer input) {
        GL20.glUniform3(location, input);
    }

    public void set(float x, float y, float z) {
        GL20.glUniform3f(location, x, y, z);
    }

    @Override
    public int type() {
        return GL20.GL_FLOAT_VEC3;
    }
}
