package lightingoverhaul.mixinmod.helper.shader;

import lightingoverhaul.mixinmod.helper.shader.common.Shader;
import lightingoverhaul.mixinmod.helper.shader.common.uniforms.floats.Uniform1F;
import lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints.Uniform1I;
import lightingoverhaul.mixinmod.helper.shader.common.uniforms.ints.Uniform4I;
import lightingoverhaul.mixinmod.helper.shader.common.uniforms.sampler.Sampler2D;

public class RGBShader extends Shader {
    public final int texCoordAttrib = getAttribLocation("TexCoord");
    public final int lightCoordAttrib = getAttribLocation("LightCoord");

    public final Uniform4I lightCoordUniform = getUniform("u_LightCoord", Uniform4I::new);
    public final Uniform4I lightCoordSunUniform = getUniform("u_LightCoordSun", Uniform4I::new);
    public final Uniform1F gammaUniform = getUniform("gamma", Uniform1F::new);
    public final Uniform1F sunLevelUniform = getUniform("sunlevel", Uniform1F::new);
    public final Uniform1F nightVisionWeightUniform = getUniform("nightVisionWeight", Uniform1F::new);
    public final Uniform1I enableTextureUniform = getUniform("enableTexture", Uniform1I::new);
    public final Uniform1I perVertexLightUniform = getUniform("perVertexLight", Uniform1I::new);
    public final Sampler2D textureUniform = getUniform("Texture", Sampler2D::new);

    public RGBShader(int program) {
        super(program);
    }
}
