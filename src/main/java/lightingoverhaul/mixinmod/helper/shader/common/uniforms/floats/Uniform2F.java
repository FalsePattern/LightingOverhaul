package lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform2F extends UniformF {
    public Uniform2F(int program, int location) {
        super(program, location);
    }

    public void set(FloatBuffer input) {
        GL20.glUniform2(location, input);
    }

    public void set(float x, float y) {
        GL20.glUniform2f(location, x, y);
    }

    @Override
    public int type() {
        return GL20.GL_FLOAT_VEC2;
    }
}
