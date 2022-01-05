package lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform4F extends UniformF {
    public Uniform4F(int program, int location) {
        super(program, location);
    }

    public void set(FloatBuffer input) {
        GL20.glUniform4(location, input);
    }

    public void set(float x, float y, float z, float w) {
        GL20.glUniform4f(location, x, y, z, w);
    }

    @Override
    public int type() {
        return GL20.GL_FLOAT_VEC4;
    }
}
