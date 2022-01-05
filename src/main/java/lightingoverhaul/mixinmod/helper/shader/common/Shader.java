package lightingoverhaul.mixinmod.helper.shader.common;

import lightingoverhaul.mixinmod.helper.shader.common.uniforms.Uniform;
import lombok.val;
import lombok.var;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Shader implements AutoCloseable {
    protected final int program;

    private int cachedShader;

    public Shader(int program) {
        this.program = program;
    }

    public void bind() {
        cachedShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(program);
    }

    public void unbind() {
        GL20.glUseProgram(cachedShader);
    }

    public <T extends Uniform> T getUniform(String name, BiFunction<Integer, Integer, T> constructor) {
        val loc = GL20.glGetUniformLocation(program, name);
        if (loc < 0) {
            throw new ShaderException("Failed to retrieve uniform location: " + name);
        }
        var id = 0;
        {
            val maxID = GL20.glGetProgrami(program, GL20.GL_ACTIVE_UNIFORMS);
            val maxLen = GL20.glGetProgrami(program, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
            var success = false;
            for (; id < maxID; id++) {
                if (name.equals(GL20.glGetActiveUniform(program, id, maxLen))) {
                    success = true;
                    break;
                }
            }
            if (!success) throw new ShaderException("Uniform type verification failed!");
        }
        val size = GL20.glGetActiveUniformSize(program, id);
        val type = GL20.glGetActiveUniformType(program, id);
        val result = constructor.apply(program, loc);
        if (result.type() != type) {
            throw new ShaderException("Failed to retrieve uniform " + name + " as " + result.getClass().getCanonicalName() + ": Type mismatch!");
        }
        if (result.size() != size) {
            throw new ShaderException("Failed to retrieve uniform " + name + " as " + result.getClass().getCanonicalName() + ": Size mismatch!");
        }
        return result;
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

        public <T extends Shader> T build(Function<Integer, T> constructor) {
            val program = ShaderHelper.createShader(sources.toArray(new ShaderSource[0]));
            return constructor.apply(program);
        }

    }
}
