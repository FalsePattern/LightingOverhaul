package coloredlightscore.src.helper;

import coloredlightscore.src.api.CLApi;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Created by Murray on 11/30/2014.
 */
public class CLChunkHelper {
    /**
     * Gets the amount of light on a block taking into account sunlight
     */
    public static int getBlockLightValue(Chunk instance, int p_76629_1_, int p_76629_2_, int p_76629_3_,
            int p_76629_4_) {
        ExtendedBlockStorage extendedblockstorage = instance.storageArrays[p_76629_2_ >> 4];

        if (extendedblockstorage == null) {
            return !instance.worldObj.provider.hasNoSky && p_76629_4_ < EnumSkyBlock.Sky.defaultLightValue
                    ? EnumSkyBlock.Sky.defaultLightValue - p_76629_4_
                    : 0;
        } else {
            int skyLight = instance.worldObj.provider.hasNoSky ? 0
                    : extendedblockstorage.getExtSkylightValue(p_76629_1_, p_76629_2_ & 15, p_76629_3_) & 0xf;

            if (skyLight > 0) {
                Chunk.isLit = true;
            }

            skyLight -= p_76629_4_;

            int sun_r = (skyLight >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int sun_g = (skyLight >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int sun_b = (skyLight >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
            int sun_l = Math.max(Math.max(sun_r, sun_g), sun_b);

            int blockLight = extendedblockstorage.getExtBlocklightValue(p_76629_1_, p_76629_2_ & 15, p_76629_3_) & 0xf;
            if (sun_l > blockLight) {
                blockLight = sun_l;
            }

            return blockLight;
        }
    }

    static int func_150808_b(Chunk instance, int paramInt1, int paramInt2, int paramInt3) {
        return instance.getBlock(paramInt1, paramInt2, paramInt3).getLightOpacity();
    }

    public static void generateSkylightMap(Chunk instance) {
        int i = instance.getTopFilledSegment();
        instance.heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                instance.precipitationHeightMap[b + (b1 << 4)] = -999;
                int j;
                for (j = i + 16 - 1; j > 0; j--) {
                    if (func_150808_b(instance, b, j - 1, b1) != 0) {
                        instance.heightMap[b1 << 4 | b] = j;
                        if (j < instance.heightMapMinimum)
                            instance.heightMapMinimum = j;
                        break;
                    }
                }
                if (!instance.worldObj.provider.hasNoSky) {
                    j = EnumSkyBlock.Sky.defaultLightValue;
                    int sun_r = (j >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                    int sun_g = (j >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                    int sun_b = (j >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
                    int k = i + 16 - 1;
                    do {
                        int m = func_150808_b(instance, b, k, b1);
                        if (m == 0 && sun_r != 15 && sun_g != 15 && sun_b != 15)
                            m = 1;
                        sun_r -= m;
                        sun_g -= m;
                        sun_b -= m;
                        if (sun_r <= 0 && sun_g <= 0 && sun_b <= 0)
                            continue;
                        ExtendedBlockStorage extendedBlockStorage = instance.storageArrays[k >> 4];
                        if (extendedBlockStorage == null)
                            continue;

                        int sun_combined = (15 << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g)
                                | (sun_b << CLApi.bitshift_sun_b);
                        extendedBlockStorage.setExtSkylightValue(b, k & 0xF, b1, sun_combined);
                        instance.worldObj.func_147479_m((instance.xPosition << 4) + b, k,
                                (instance.zPosition << 4) + b1);
                        --k;
                    } while (k > 0 && (sun_r > 0 || sun_g > 0 || sun_b > 0));
                }
            }
        }
        instance.isModified = true;
    }

    public static void relightBlock(Chunk instance, int x, int y, int z) {
        int i = instance.heightMap[z << 4 | x] & 0xFF;
        int j = i;
        if (y > i)
            j = y;
        while (j > 0 && func_150808_b(instance, x, j - 1, z) == 0)
            j--;
        if (j == i)
            return;
        instance.worldObj.markBlocksDirtyVertical(x + instance.xPosition * 16, z + instance.zPosition * 16, j, i);
        instance.heightMap[z << 4 | x] = j;
        int k = instance.xPosition * 16 + x;
        int m = instance.zPosition * 16 + z;
        if (!instance.worldObj.provider.hasNoSky) {
            if (j < i) {
                for (int i4 = j; i4 < i; i4++) {
                    ExtendedBlockStorage extendedBlockStorage = instance.storageArrays[i4 >> 4];
                    if (extendedBlockStorage != null) {
                        extendedBlockStorage.setExtSkylightValue(x, i4 & 0xF, z, EnumSkyBlock.Sky.defaultLightValue);
                        instance.worldObj.func_147479_m((instance.xPosition << 4) + x, i4,
                                (instance.zPosition << 4) + z);
                    }
                }
            } else {
                for (int i4 = i; i4 < j; i4++) {
                    ExtendedBlockStorage extendedBlockStorage = instance.storageArrays[i4 >> 4];
                    if (extendedBlockStorage != null) {
                        extendedBlockStorage.setExtSkylightValue(x, i4 & 0xF, z, 0);
                        instance.worldObj.func_147479_m((instance.xPosition << 4) + x, i4,
                                (instance.zPosition << 4) + z);
                    }
                }
            }
            int i3 = EnumSkyBlock.Sky.defaultLightValue;
            int sun_r = (i3 >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int sun_g = (i3 >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int sun_b = (i3 >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
            while (j > 0 && (sun_r > 0 || sun_g > 0 || sun_b > 0)) {
                j--;
                int i4 = func_150808_b(instance, x, j, z);
                if (i4 == 0)
                    i4 = 1;
                sun_r -= i4;
                sun_g -= i4;
                sun_b -= i4;
                sun_r = Math.max(0, sun_r);
                sun_g = Math.max(0, sun_g);
                sun_b = Math.max(0, sun_b);
                int sun_combined = (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g)
                        | (sun_b << CLApi.bitshift_sun_b);
                ExtendedBlockStorage extendedBlockStorage = instance.storageArrays[j >> 4];
                if (extendedBlockStorage != null)
                    extendedBlockStorage.setExtSkylightValue(x, j & 0xF, z, sun_combined);
            }
        }
        int n = instance.heightMap[z << 4 | x];
        int i1 = i;
        int i2 = n;
        if (i2 < i1) {
            int i3 = i1;
            i1 = i2;
            i2 = i3;
        }
        if (n < instance.heightMapMinimum)
            instance.heightMapMinimum = n;
        if (!instance.worldObj.provider.hasNoSky) {
            instance.updateSkylightNeighborHeight(k - 1, m, i1, i2);
            instance.updateSkylightNeighborHeight(k + 1, m, i1, i2);
            instance.updateSkylightNeighborHeight(k, m - 1, i1, i2);
            instance.updateSkylightNeighborHeight(k, m + 1, i1, i2);
            instance.updateSkylightNeighborHeight(k, m, i1, i2);
        }
        instance.isModified = true;
    }
}
