package com.lightingoverhaul.mixinmod.helper.shader.common;

import com.lightingoverhaul.mixinmod.helper.shader.RGBShader;
import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

public class Shader implements AutoCloseable {
    protected final int program;

    private int cachedShader;

    public Shader(ShaderSource... shaders) {
        program = ShaderHelper.createShader(shaders);
    }

    public void bind() {
        cachedShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(program);
    }

    public void unbind() {
        GL20.glUseProgram(cachedShader);
    }

    public int getUniformLocation(String name) {
        val loc = GL20.glGetUniformLocation(program, name);
        if (loc < 0) {
            throw new ShaderException("Failed to retrieve uniform location: " + name);
        }
        return loc;
    }

    public int getAttribLocation(String name) {
        val loc = GL20.glGetAttribLocation(program, name);
        if (loc < 0) {
            throw new ShaderException("Failed to retrieve attribute location: " + name);
        }
        return loc;
    }

    @Override
    public void close() {
        GL20.glDeleteProgram(program);
    }

    public static Shader.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected final List<ShaderSource> sources = new ArrayList<>();
        public Builder(){}

        public Builder addVertex(String source) {
            sources.add(new ShaderSource(source, ShaderSource.Type.VERTEX));
            return this;
        }

        public Builder addGeometry(String source) {
            sources.add(new ShaderSource(source, ShaderSource.Type.GEOMETRY));
            return this;
        }

        public Builder addFragment(String source) {
            sources.add(new ShaderSource(source, ShaderSource.Type.FRAGMENT));
            return this;
        }

        public Shader build() {
            return new Shader(sources.toArray(new ShaderSource[0]));
        }

    }
}
