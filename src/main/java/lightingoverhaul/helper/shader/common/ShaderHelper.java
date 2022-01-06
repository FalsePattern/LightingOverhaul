package lightingoverhaul.helper.shader.common;

import lightingoverhaul.CoreLoadingPlugin;
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

    private static int compileShader(String source, int shaderType) {
        val shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        val infoLog = GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, GL20.GL_INFO_LOG_LENGTH));
        if (GL11.glGetError() != GL11.GL_NO_ERROR || GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE) {
            GL20.glDeleteShader(shader);
            throw new ShaderException("Error while compiling shader: \n" + (infoLog.equals("") ? "No Details Available" : infoLog));
        } else if (!"".equals(infoLog)) {
            CoreLoadingPlugin.CLLog.warn("Warnings detected while compiling shader:");
            for (val line: infoLog.split("\\n")) {
                CoreLoadingPlugin.CLLog.warn(line);
            }
        }
        return shader;
    }

    private static int createProgram(int... shaders) {
        val program = GL20.glCreateProgram();
        for (val shader: shaders) {
            GL20.glAttachShader(program, shader);
        }
        GL20.glLinkProgram(program);
        verifyProgram(program, GL20.GL_LINK_STATUS, "Error while linking shader program");

        GL20.glValidateProgram(program);
        verifyProgram(program, GL20.GL_VALIDATE_STATUS, "Error while validating shader program");

        for (val shader: shaders) {
            GL20.glDetachShader(program, shader);
        }
        return program;
    }

    private static void verifyProgram(int program, int programI, String message) {
        val status = GL20.glGetProgrami(program, programI);
        val infoLog = GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, GL20.GL_INFO_LOG_LENGTH));
        if (GL11.glGetError() != GL11.GL_NO_ERROR || status != GL11.GL_TRUE) {
            GL20.glDeleteProgram(program);
            throw new ShaderException(message + ": \n" + (infoLog.equals("") ? "No Details Available" : infoLog));
        } else if (!"".equals(infoLog)) {
            CoreLoadingPlugin.CLLog.warn("Warnings detected while linking or verifying shader:");
            for (val line: infoLog.split("\\n")) {
                CoreLoadingPlugin.CLLog.warn(line);
            }
        }
    }

}
