#version 130

uniform sampler2D Texture;
uniform sampler2D LightMap;
uniform ivec4 u_LightCoord;
uniform ivec4 u_LightCoordSun;
attribute vec2 TexCoord;
varying vec2 p_TexCoord;
attribute vec4 LightCoord;
varying vec4 p_LightCoord;
varying vec4 p_LightCoordSun;
varying vec4 p_Color;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    p_TexCoord = TexCoord;
    p_Color = gl_Color;

    if (u_LightCoord == ivec4(0, 0, 0, 0) && u_LightCoordSun == ivec4(0, 0, 0, 0)) {
	    ivec4 color_mixed = ivec4(
			int(LightCoord.x),
			int(LightCoord.y),
			int(LightCoord.z),
			int(LightCoord.w)
		);
		vec4 color_block = vec4(
			float(color_mixed.x & 0xf),
			float(color_mixed.x >> 4),
			float(color_mixed.y & 0xf),
			0
		);
		vec4 color_sun = vec4(
			float(color_mixed.z & 0xf),
			float(color_mixed.z >> 4),
			float(color_mixed.w & 0xf),
			0
		);
        p_LightCoord = color_block;
		p_LightCoordSun = color_sun;
    } else {
        p_LightCoord = u_LightCoord;
        p_LightCoordSun = u_LightCoordSun;
    }
}
