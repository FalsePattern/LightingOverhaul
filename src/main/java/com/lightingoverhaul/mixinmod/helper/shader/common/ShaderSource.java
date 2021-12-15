package com.lightingoverhaul.mixinmod.helper.shader.common;

import lombok.Builder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

public class ShaderSource {
    public final String source;
    public final Type type;

    @Builder
    public ShaderSource(String source, Type type) {
        this.source = source;
        this.type = type;
    }

    public enum Type {
        VERTEX, GEOMETRY, FRAGMENT;

        public int toGL() {
            switch (this) {
                case VERTEX: return GL20.GL_VERTEX_SHADER;
                case GEOMETRY: return GL32.GL_GEOMETRY_SHADER;
                case FRAGMENT: return GL20.GL_FRAGMENT_SHADER;
                default: return GL11.GL_FALSE;
            }
        }
    }
}
