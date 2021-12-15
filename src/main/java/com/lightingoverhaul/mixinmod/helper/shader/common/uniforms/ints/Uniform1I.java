package com.lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints;

import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class Uniform1I extends UniformI {
    public Uniform1I(int program, int location) {
        super(program, location);
    }

    public void set(IntBuffer values) {
        GL20.glUniform1(location, values);
    }

    public void set(int x) {
        GL20.glUniform1i(location, x);
    }
}
