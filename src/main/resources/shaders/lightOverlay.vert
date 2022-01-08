#version 130

uniform sampler2D Texture;
uniform ivec4 u_LightCoord;
uniform ivec4 u_LightCoordSun;
uniform int perVertexLight;
attribute vec2 TexCoord;
varying vec2 p_TexCoord;
attribute vec4 LightCoord;
varying vec4 p_LightCoord;
varying vec4 p_LightCoordSun;
varying vec4 p_Color;

#define BLOCK_R x & 0xf
#define BLOCK_G x >> 4
#define BLOCK_B y & 0xf
#define SUN_R y >> 4
#define SUN_G z & 0xf
#define SUN_B z >> 4
void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    p_TexCoord = TexCoord;
    p_Color = gl_Color;

    if (perVertexLight == 1) {
	    ivec4 color_mixed = ivec4(
			int(LightCoord.x),
			int(LightCoord.y),
			int(LightCoord.z),
			int(LightCoord.w)
		);
		vec4 color_block = vec4(
			float(color_mixed.BLOCK_R),
			float(color_mixed.BLOCK_G),
			float(color_mixed.BLOCK_B),
			0
		);
		vec4 color_sun = vec4(
			float(color_mixed.SUN_R),
			float(color_mixed.SUN_G),
			float(color_mixed.SUN_B),
			0
		);
        p_LightCoord = color_block;
		p_LightCoordSun = color_sun;
    } else {
        p_LightCoord = u_LightCoord;
        p_LightCoordSun = u_LightCoordSun;
    }
}
