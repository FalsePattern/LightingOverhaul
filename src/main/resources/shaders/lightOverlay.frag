#version 130

uniform sampler2D Texture;
varying vec2 p_TexCoord;
varying vec4 p_Color;
varying vec4 p_LightCoord;
varying vec4 p_LightCoordSun;
uniform float gamma;
uniform vec3 sunColor;
uniform float nightVisionWeight;
uniform int enableTexture;

vec3 toBrightness(vec3 light) {
    vec3 v1 = 1.0 - light / 15.0;
    return clamp((1.0 - v1) / (v1 * 3.0 + 1.0), 0.0, 1.0);
}

vec3 applyGammaV(vec3 light) {
    vec3 lightC;
    light = clamp(light, 0.0, 1.0);
    lightC = 1.0 - light;
    lightC = 1.0 - pow(lightC, vec3(4));
    light = light * (1.0 - gamma) + lightC * gamma;
    light = 0.96 * light + 0.03;
    light = clamp(light, 0.0, 1.0);
    return light;
}

vec3 doRGB(vec3 block, vec3 sun) {
    float Min = 0.05;
    float Max = 1.0;
    float nightVisionMinBrightness = 0.7;
    Min = Min * (1.0 - nightVisionWeight) + nightVisionMinBrightness * nightVisionWeight;
    vec3 block_bright = toBrightness(block);
    vec3 sun_bright = (sunColor * toBrightness(sun));
    vec3 brightness = sun_bright + block_bright;
    brightness = clamp(brightness, vec3(0), vec3(1));
    return applyGammaV(brightness) * (Max - Min) + Min;
}

void main() {
    vec4 color = vec4(doRGB(p_LightCoord.xyz, p_LightCoordSun.xyz), 1);
    if (enableTexture == 1)
    {
        gl_FragColor = texture2D(Texture, p_TexCoord) * p_Color * (color);
    }
    else
    {
        gl_FragColor = p_Color * (color);
    }
}
