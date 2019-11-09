package coloredlightscore.src.helper;

import static coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin.CLLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.*;

import coloredlightscore.src.api.CLApi;

public class CLTessellatorHelper {
    //private static int nativeBufferSize = 0x200000;
    public static int texCoordParam;
    public static int lightCoordParam;
    public static int clProgram;
    private static boolean programInUse;
    public static int lightCoordUniform;
    public static int lightCoordSunUniform;
    public static int gammaUniform;
    public static int sunlevelUniform;
    public static int nightVisionWeightUniform;
    private static IntBuffer cachedLightCoord;
    private static IntBuffer cachedLightCoordSun;
    private static int cachedShader;
    private static boolean hasFlaggedOpenglError;
    private static int lastGLErrorCode = GL11.GL_NO_ERROR;
    private static String infoStr;
    public static boolean lockedBrightness;
    public static float gamma;
    public static float sunlevel;
    public static float nightVisionWeight;

    static {
        cachedLightCoord = ByteBuffer.allocateDirect(16).asIntBuffer();
        cachedLightCoordSun = ByteBuffer.allocateDirect(16).asIntBuffer();
        cachedShader = 0;
        hasFlaggedOpenglError = false;
    }

    public CLTessellatorHelper() {
    }

    public static void setBrightness(Tessellator instance, int par1) {
        if (lockedBrightness)
            return;
        instance.hasBrightness = true;
        instance.brightness = par1;
        if (par1 < 256)
            instance.brightness = makeBrightness(par1);
    }

    public static void setupShaders() {
        int vertShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fragShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);

        String vertSource = readResourceAsString("/shaders/lightOverlay.vert");
        String fragSource = readResourceAsString("/shaders/lightOverlay.frag");

        GL20.glShaderSource(vertShader, vertSource);
        GL20.glShaderSource(fragShader, fragSource);


        GL20.glCompileShader(vertShader);
        infoStr = GL20.glGetShaderInfoLog(vertShader, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error(vertSource);
            CLLog.error("Compiling vertShader");
            CLLog.error(infoStr);
        } else if (infoStr != "") {
            CLLog.info(vertSource);
            CLLog.info("Compiling vertShader");
            CLLog.info(infoStr);
        }

        GL20.glCompileShader(fragShader);
        infoStr = GL20.glGetShaderInfoLog(fragShader, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error(fragSource);
            CLLog.error("Compiling fragShader");
            CLLog.error(infoStr);
        } else if (infoStr != "") {
            CLLog.info(fragSource);
            CLLog.info("Compiling fragShader");
            CLLog.info(infoStr);
        }

        clProgram = GL20.glCreateProgram();
        GL20.glAttachShader(clProgram, vertShader);
        GL20.glAttachShader(clProgram, fragShader);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error("Error attaching shaders");
        }



        GL20.glLinkProgram(clProgram);
        infoStr = GL20.glGetProgramInfoLog(clProgram, 2000);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error("Linking Program");
            CLLog.error(infoStr);
        } else if (infoStr != "") {
            CLLog.info("Linking Program");
            CLLog.info(infoStr);
        }
        GL20.glDetachShader(clProgram, vertShader);
        GL20.glDetachShader(clProgram, fragShader);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error("Error detaching shaders");
        }

        GL20.glDeleteShader(vertShader);
        GL20.glDeleteShader(fragShader);
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            CLLog.error("Error deleting shaders (WHAT DID YOU DO?!?)");
        }

        texCoordParam = GL20.glGetAttribLocation(clProgram, "TexCoord");
        lightCoordParam = GL20.glGetAttribLocation(clProgram, "LightCoord");
        lightCoordUniform = GL20.glGetUniformLocation(clProgram, "u_LightCoord");
        lightCoordSunUniform = GL20.glGetUniformLocation(clProgram, "u_LightCoordSun");
        gammaUniform = GL20.glGetUniformLocation(clProgram, "gamma");
        sunlevelUniform = GL20.glGetUniformLocation(clProgram, "sunlevel");
        nightVisionWeightUniform = GL20.glGetUniformLocation(clProgram, "nightVisionWeight");

        if (texCoordParam <= 0) {
            CLLog.error("texCoordParam attribute location returned: " + texCoordParam);
        }
        if (lightCoordParam <= 0) {
            CLLog.error("lightCoordParam attribute location returned: " + lightCoordParam);
        }
        if (lightCoordUniform <= 0) {
            CLLog.error("lightCoordUniform attribute location returned: " + lightCoordUniform);
        }
        if (lightCoordSunUniform <= 0) {
            CLLog.error("lightCoordSunUniform attribute location returned: " + lightCoordSunUniform);
        }
        if (gammaUniform <= 0) {
            CLLog.error("gammaUniform attribute location returned: " + gammaUniform);
        }
        if (sunlevelUniform <= 0) {
            CLLog.error("sunlevelUniform attribute location returned: " + sunlevelUniform);
        }
        if (nightVisionWeight <= 0) {
            CLLog.error("nightVisionWeight attribute location returned: " + nightVisionWeight);
        }
    }

    private static String readResourceAsString(String path) {
        InputStream is = CLTessellatorHelper.class.getResourceAsStream(path);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder source = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                    source.append(line + "\n");
                }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source.toString();
    }

    public static void enableShader() {
        cachedShader = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(clProgram);
        programInUse = true;
        int textureUniform = GL20.glGetUniformLocation(clProgram, "Texture");
        GL20.glUniform1i(textureUniform, OpenGlHelper.defaultTexUnit - GL13.GL_TEXTURE0);
        int lightmapUniform = GL20.glGetUniformLocation(clProgram, "LightMap");
        GL20.glUniform1i(lightmapUniform, OpenGlHelper.lightmapTexUnit - GL13.GL_TEXTURE0);
        GL20.glUniform1f(gammaUniform, gamma);
        GL20.glUniform1f(sunlevelUniform, sunlevel);
        GL20.glUniform1f(nightVisionWeightUniform, nightVisionWeight);
    }

    public static void disableShader() {
        programInUse = false;
        GL20.glUseProgram(cachedShader);
    }

    public static void setTextureCoord(FloatBuffer buffer) {
        lastGLErrorCode = GL11.glGetError();
        if (lastGLErrorCode != GL11.GL_NO_ERROR) {
            if (!hasFlaggedOpenglError) {
                CLLog.warn("Render error entering CLTessellatorHelper.setTextureCoord()! Error Code: " + lastGLErrorCode + ". Trying to proceed anyway...");
                hasFlaggedOpenglError = true;
            }
        }
        GL20.glVertexAttribPointer(texCoordParam, 2, false, 32, buffer);
        GL20.glEnableVertexAttribArray(texCoordParam);
    }

    public static void unsetTextureCoord() {
        GL20.glDisableVertexAttribArray(texCoordParam);
    }

    public static void updateShaders(float newGamma, float newSunlevel, float newNightVisionWeight)
    {
        gamma = newGamma;
        sunlevel = newSunlevel;
        nightVisionWeight = newNightVisionWeight;
    }

    public static void setLightCoord(ByteBuffer buffer) {
        GL20.glGetUniform(clProgram, lightCoordUniform, cachedLightCoord);
        GL20.glUniform4i(lightCoordUniform, 0, 0, 0, 0);
        GL20.glGetUniform(clProgram, lightCoordSunUniform, cachedLightCoordSun);
        GL20.glUniform4i(lightCoordSunUniform, 0, 0, 0, 0);
        GL20.glVertexAttribPointer(lightCoordParam, 4, true, false, 32, buffer);
        GL20.glEnableVertexAttribArray(lightCoordParam);
    }

    public static void unsetLightCoord() {
        GL20.glDisableVertexAttribArray(lightCoordParam);
        GL20.glUniform4(lightCoordUniform, cachedLightCoord);
        GL20.glUniform4(lightCoordSunUniform, cachedLightCoordSun);
    }

    public static int makeBrightness(int lightlevel)
    {
        return lightlevel << CLApi.bitshift_l2 | lightlevel << CLApi.bitshift_r2 | lightlevel << CLApi.bitshift_g2 | lightlevel << CLApi.bitshift_b2;
    }

    public static void addVertex(Tessellator instance, double par1, double par3, double par5) {
        int cl_rawBufferSize = instance.getRawBufferSize();

        if (instance.rawBufferIndex >= cl_rawBufferSize - 32) {
            if (cl_rawBufferSize == 0) {
                cl_rawBufferSize = 0x10000; //65536
                instance.setRawBufferSize(cl_rawBufferSize);
                instance.rawBuffer = new int[cl_rawBufferSize];
            } else {
                cl_rawBufferSize *= 2;
                instance.setRawBufferSize(cl_rawBufferSize);
                instance.rawBuffer = Arrays.copyOf(instance.rawBuffer, cl_rawBufferSize);
            }
        }

        ++instance.addedVertices;

        if (instance.hasTexture) {
            instance.rawBuffer[instance.rawBufferIndex + 3] = Float.floatToRawIntBits((float) instance.textureU);
            instance.rawBuffer[instance.rawBufferIndex + 4] = Float.floatToRawIntBits((float) instance.textureV);
        }

        if (instance.hasBrightness) {
            /* << and >> take precedence over &
             * Incoming:
             * 0000 0000 SSSS BBBB GGGG RRRR LLLL 0000 */
            int l = (instance.brightness >> CLApi.bitshift_l2 ) & 0xF;
            int block_r = (instance.brightness >> CLApi.bitshift_r2 ) & CLApi.bitmask;
            int block_g = (instance.brightness >> CLApi.bitshift_g2 ) & CLApi.bitmask;
            int block_b = (instance.brightness >> CLApi.bitshift_b2 ) & CLApi.bitmask;
            int s = (instance.brightness >> CLApi.bitshift_s2 ) & 0xF;

            block_r = Math.min(15, block_r);
            block_g = Math.min(15, block_g);
            block_b = Math.min(15, block_b);

            int sun_r = s;
            int sun_g = s;
            int sun_b = s;
            
            /* 0000 SSSS 0000 BBBB 0000 GGGG 0000 RRRR */
            instance.rawBuffer[instance.rawBufferIndex + 7] = (block_r << 0)
                                                            | (block_g << 4)
                                                            | (block_b << 8)
                                                            | (sun_r << 16)
                                                            | (sun_g << 20)
                                                            | (sun_b << 24);
        }

        if (instance.hasColor) {
            instance.rawBuffer[instance.rawBufferIndex + 5] = instance.color;
        }

        if (instance.hasNormals) {
            instance.rawBuffer[instance.rawBufferIndex + 6] = instance.normal;
        }

        instance.rawBuffer[instance.rawBufferIndex + 0] = Float.floatToRawIntBits((float) (par1 + instance.xOffset));
        instance.rawBuffer[instance.rawBufferIndex + 1] = Float.floatToRawIntBits((float) (par3 + instance.yOffset));
        instance.rawBuffer[instance.rawBufferIndex + 2] = Float.floatToRawIntBits((float) (par5 + instance.zOffset));
        instance.rawBufferIndex += 8;
        ++instance.vertexCount;

        return;

    }

    public static boolean isProgramInUse() {
        return programInUse;
    }
}
