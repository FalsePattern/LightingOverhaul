package lightingoverhaul.helper.shader.common.uniforms.ints;

import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class Uniform2I extends UniformI {
    public Uniform2I(int program, int location) {
        super(program, location);
    }

    public void set(IntBuffer input) {
        GL20.glUniform2(location, input);
    }

    public void set(int x, int y) {
        GL20.glUniform2i(location, x, y);
    }

    @Override
    public int type() {
        return GL20.GL_INT_VEC2;
    }
}
