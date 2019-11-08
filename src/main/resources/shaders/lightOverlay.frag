#version 120

uniform sampler2D Texture;
uniform sampler2D LightMap;
varying vec2 p_TexCoord;
varying vec4 p_Color;
varying vec4 p_LightCoord;
uniform float gamma;
uniform float sunlevel;
uniform float nightVisionWeight;

float getBrightness(float lightlevel)
{
    float f1 = 1.0 - lightlevel / 15.0;
    return (1.0 - f1) / (f1 * 3.0 + 1.0);
}

float applyGamma(float light)
{
    float lightC;
    light = clamp(light, 0.0, 1.0);
    lightC = 1.0 - light;
    light = light * (1.0 - gamma) + (1.0 - lightC * lightC * lightC * lightC) * gamma;
    light = 0.96 * light + 0.03;
    light = clamp(light, 0.0, 1.0);
    return light;
}

float normalize(float f)
{
    return f;
	/* return (f - 0.2) * 1.25; */
}

float doColor(float light, float sunpart)
{
    float min = 0.05;
    float max = 1.0;
    float nightVisionMinBrightness = 0.7;
    min = min * (1.0 - nightVisionWeight) + nightVisionMinBrightness * nightVisionWeight;
    float brightness = getBrightness(light) + normalize(sunlevel) * getBrightness(sunpart);
    return applyGamma(brightness) * (max - min) + min;
}

void main() {
    float lightlevel_r = p_LightCoord.x;
    float lightlevel_g = p_LightCoord.y;
    float lightlevel_b = p_LightCoord.z;
    float lightlevel_s = p_LightCoord.w;

    float brightness_r = doColor(lightlevel_r, lightlevel_s);
    float brightness_g = doColor(lightlevel_g, lightlevel_s);
    float brightness_b = doColor(lightlevel_b, lightlevel_s);

    vec4 color = vec4(brightness_r, brightness_g, brightness_b, 1);
    gl_FragColor = texture2D(Texture, p_TexCoord) * p_Color * (color);
}
