package com.lightingoverhaul.mixinmod.helper;

import com.lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderHelper {
    public static int createShader(String vertexSource, String fragmentSource) {
        int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
        try {
            int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);
            try {
                return createProgram(vertexShader, fragmentShader);
            } finally {
                GL20.glDeleteShader(fragmentShader);
            }
        } finally {
            GL20.glDeleteShader(vertexShader);
        }
    }

    public static int compileShader(String source, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        String infoString = GL20.glGetShaderInfoLog(shader, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR || !infoString.equals("")) {
            GL20.glDeleteShader(shader);
            throw new ShaderException("Error while compiling shader: \n" + infoString);
        }
        return shader;
    }

    public static int createProgram(int... shaders) {
        int program = GL20.glCreateProgram();
        for (int shader: shaders) {
            GL20.glAttachShader(program, shader);
        }
        GL20.glLinkProgram(program);
        for (int shader: shaders) {
            GL20.glDetachShader(program, shader);
        }
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new ShaderException("Error detaching shaders");
        }
        String infoString = GL20.glGetProgramInfoLog(program, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR || !infoString.equals("")) {
            GL20.glDeleteProgram(program);
            throw new ShaderException("Error while linking shader program: \n" + infoString);
        }
        GL20.glValidateProgram(program);
        infoString = GL20.glGetProgramInfoLog(program, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR || !infoString.equals("")) {
            GL20.glDeleteProgram(program);
            throw new ShaderException("Error while validating shader program: \n" + infoString);
        }
        return program;
    }

}
