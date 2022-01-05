package lightingoverhaul.helper.shader.common.uniforms.ints;

import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class Uniform3I extends UniformI {

    public Uniform3I(int program, int location) {
        super(program, location);
    }

    public void set(IntBuffer input) {
        GL20.glUniform3(location, input);
    }

    public void set(int x, int y, int z) {
        GL20.glUniform3i(location, x, y, z);
    }

    @Override
    public int type() {
        return GL20.GL_INT_VEC3;
    }
}
