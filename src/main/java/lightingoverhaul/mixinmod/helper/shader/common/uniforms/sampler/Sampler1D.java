package lightingoverhaul.mixinmod.helper.shader.common.uniforms.sampler;

import lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints.Uniform1I;
import org.lwjgl.opengl.GL20;

public class Sampler1D extends Uniform1I {
    public Sampler1D(int program, int location) {
        super(program, location);
    }

    @Override
    public int type() {
        return GL20.GL_SAMPLER_1D;
    }
}
