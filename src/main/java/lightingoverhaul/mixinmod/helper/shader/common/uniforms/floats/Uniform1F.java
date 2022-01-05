package lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class Uniform1F extends UniformF {
    public Uniform1F(int program, int location) {
        super(program, location);
    }

    public void set(FloatBuffer input) {
        GL20.glUniform1(location, input);
    }

    public void set(float x) {
        GL20.glUniform1f(location, x);
    }

    @Override
    public int type() {
        return GL11.GL_FLOAT;
    }
}
