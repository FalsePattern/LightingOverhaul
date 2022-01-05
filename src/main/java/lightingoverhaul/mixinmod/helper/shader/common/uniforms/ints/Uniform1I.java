package lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public class Uniform1I extends UniformI {
    public Uniform1I(int program, int location) {
        super(program, location);
    }

    public void set(IntBuffer values) {
        GL20.glUniform1(location, values);
    }

    public void set(int x) {
        GL20.glUniform1i(location, x);
    }

    @Override
    public int type() {
        return GL11.GL_INT;
    }
}
