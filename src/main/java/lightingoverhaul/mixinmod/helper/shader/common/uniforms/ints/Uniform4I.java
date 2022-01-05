package lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints;

import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class Uniform4I extends UniformI {
    public Uniform4I(int program, int location) {
        super(program, location);
    }

    public void set(IntBuffer input) {
        GL20.glUniform3(location, input);
    }

    public void set(int x, int y, int z, int w) {
        GL20.glUniform4i(location, x, y, z, w);
    }

    @Override
    public int type() {
        return GL20.GL_INT_VEC4;
    }
}
