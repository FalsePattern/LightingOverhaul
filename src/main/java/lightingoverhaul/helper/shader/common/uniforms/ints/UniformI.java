package lightingoverhaul.helper.shader.common.uniforms.ints;

import lightingoverhaul.helper.shader.common.uniforms.Uniform;
import org.lwjgl.opengl.GL20;

import java.nio.IntBuffer;

public abstract class UniformI extends Uniform {
    public UniformI(int program, int location) {
        super(program, location);
    }

    public abstract void set(IntBuffer input);

    public void get(IntBuffer output) {
        GL20.glGetUniform(program, location, output);
    }
}
