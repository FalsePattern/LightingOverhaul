package lightingoverhaul.coremod.helper.shader.common;

import lombok.val;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderHelper {
    public static int createShader(ShaderSource... sources) {
        val shaders = new int[sources.length];
        int createdShaders = 0;
        try {
            for (int i = 0; i < sources.length; i++) {
                val source = sources[i];
                val shader = compileShader(source.source, source.type.toGL());
                createdShaders++;
                shaders[i] = shader;
            }
            return createProgram(shaders);
        } finally {
            for (int i = createdShaders - 1; i >= 0; i--) {
                GL20.glDeleteShader(shaders[i]);
            }
        }
    }

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

    private static int compileShader(String source, int shaderType) {
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        String infoString = GL20.glGetShaderInfoLog(shader, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR || GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE || !infoString.equals("")) {
            GL20.glDeleteShader(shader);
            throw new ShaderException("Error while compiling shader: \n" + infoString);
        }
        return shader;
    }

    private static int createProgram(int... shaders) {
        int program = GL20.glCreateProgram();
        for (int shader: shaders) {
            GL20.glAttachShader(program, shader);
        }
        GL20.glLinkProgram(program);
        verifyProgram(program, GL20.GL_LINK_STATUS, "Error while linking shader program");

        GL20.glValidateProgram(program);
        verifyProgram(program, GL20.GL_VALIDATE_STATUS, "Error while validating shader program");

        for (int shader: shaders) {
            GL20.glDetachShader(program, shader);
        }
        return program;
    }

    private static void verifyProgram(int program, int programI, String message) {
        String infoLog = GL20.glGetProgramInfoLog(program, 2000);
        int status = GL20.glGetProgrami(program, programI);
        if (GL11.glGetError() != GL11.GL_NO_ERROR || status != GL11.GL_TRUE || !infoLog.equals("")) {
            GL20.glDeleteProgram(program);
            throw new ShaderException(message + ": \n" + (infoLog.equals("") ? "No Details Available" : infoLog));
        }
    }

}
