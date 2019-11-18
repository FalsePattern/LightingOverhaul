package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import coloredlightscore.src.api.CLApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(Chunk.class)
public abstract class ChunkMixin {
    @Shadow
    public World worldObj;

    @Shadow
    public ExtendedBlockStorage[] storageArrays;

    @Shadow
    private int func_150808_b(int x, int y, int z) {
        return 0;
    }

    @Shadow
    public int xPosition;

    @Shadow
    public int zPosition;

    @Shadow
    public boolean isModified;

    @Shadow
    public int getTopFilledSegment() {
        return 0;
    }

    @Shadow
    public int heightMapMinimum;

    @Shadow
    public int[] heightMap;

    @Shadow
    public int[] precipitationHeightMap;

    @Shadow
    private void updateSkylightNeighborHeight(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public int getBlockLightValue(int x, int y, int z, int value) {
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];

        if (extendedblockstorage == null) {
            return !this.worldObj.provider.hasNoSky && value < EnumSkyBlock.Sky.defaultLightValue ? EnumSkyBlock.Sky.defaultLightValue - value : 0;
        } else {
            int skyLight = this.worldObj.provider.hasNoSky ? 0 : extendedblockstorage.getExtSkylightValue(x, y & 15, z) & 0xf;

            if (skyLight > 0) {
                Chunk.isLit = true;
            }

            skyLight -= value;

            int blockLight = extendedblockstorage.getExtBlocklightValue(x, y & 15, z) & 0xf;
            if (skyLight > blockLight) {
                blockLight = skyLight;
            }

            return blockLight;
        }
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public void generateSkylightMap() {
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                this.precipitationHeightMap[b + (b1 << 4)] = -999;
                int j;
                for (j = i + 16 - 1; j > 0; j--) {
                    if (func_150808_b(b, j - 1, b1) != 0) {
                        this.heightMap[b1 << 4 | b] = j;
                        if (j < this.heightMapMinimum)
                            this.heightMapMinimum = j;
                        break;
                    }
                }
                if (!this.worldObj.provider.hasNoSky) {
                    j = EnumSkyBlock.Sky.defaultLightValue;
                    int sun_r = (j >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                    int sun_g = (j >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                    int sun_b = (j >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
                    int k = i + 16 - 1;
                    do {
                        int m = func_150808_b(b, k, b1);
                        if (m == 0 && sun_r != 15 && sun_g != 15 && sun_b != 15)
                            m = 1;
                        sun_r -= m;
                        sun_g -= m;
                        sun_b -= m;
                        if (sun_r <= 0 && sun_g <= 0 && sun_b <= 0)
                            continue;
                        ExtendedBlockStorage extendedBlockStorage = this.storageArrays[k >> 4];
                        if (extendedBlockStorage != null) {
                            int sun_combined = (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g) | (sun_b << CLApi.bitshift_sun_b);
                            extendedBlockStorage.setExtSkylightValue(b, k & 0xF, b1, sun_combined);
                            this.worldObj.func_147479_m((this.xPosition << 4) + b, k, (this.zPosition << 4) + b1);
                        }
                        --k;
                    } while (k > 0 && (sun_r > 0 || sun_g > 0 || sun_b > 0));
                }
            }
        }
        this.isModified = true;
    }

    /***
     * @author darkshadow44
     * @reason We need relightBlock to be called when glass is placed or removed...
     */
    @Redirect(method = "func_150807_a", at = @At(value = "INVOKE", target = "net.minecraft.block.Block.getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I", ordinal = 1))
    private int func_150807_a_getLightOpacity(Block instance, IBlockAccess world, int x, int y, int z) {
        if (instance instanceof BlockStainedGlass) {
            return 1;
        }
        return instance.getLightOpacity(world, x, y, z);
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    private void relightBlock(int x, int y, int z) {
        int i = this.heightMap[z << 4 | x] & 0xFF;
        int j = i;
        if (y > i)
            j = y;
        while (j > 0 && func_150808_b(x, j - 1, z) == 0)
            j--;
        if (j == i)
            return;
        this.worldObj.markBlocksDirtyVertical(x + this.xPosition * 16, z + this.zPosition * 16, j, i);
        this.heightMap[z << 4 | x] = j;
        int k = this.xPosition * 16 + x;
        int m = this.zPosition * 16 + z;
        if (!this.worldObj.provider.hasNoSky) {
            if (j < i) {
                for (int i4 = j; i4 < i; i4++) {
                    ExtendedBlockStorage extendedBlockStorage = this.storageArrays[i4 >> 4];
                    if (extendedBlockStorage != null) {
                        extendedBlockStorage.setExtSkylightValue(x, i4 & 0xF, z, EnumSkyBlock.Sky.defaultLightValue);
                        this.worldObj.func_147479_m((this.xPosition << 4) + x, i4, (this.zPosition << 4) + z);
                    }
                }
            } else {
                for (int i4 = i; i4 < j; i4++) {
                    ExtendedBlockStorage extendedBlockStorage = this.storageArrays[i4 >> 4];
                    if (extendedBlockStorage != null) {
                        extendedBlockStorage.setExtSkylightValue(x, i4 & 0xF, z, 0);
                        this.worldObj.func_147479_m((this.xPosition << 4) + x, i4, (this.zPosition << 4) + z);
                    }
                }
            }
            int i3 = EnumSkyBlock.Sky.defaultLightValue;
            int sun_r = (i3 >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int sun_g = (i3 >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int sun_b = (i3 >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
            while (j > 0 && (sun_r > 0 || sun_g > 0 || sun_b > 0)) {
                j--;
                int i4 = func_150808_b(x, j, z);
                if (i4 == 0)
                    i4 = 1;
                sun_r -= i4;
                sun_g -= i4;
                sun_b -= i4;
                sun_r = Math.max(0, sun_r);
                sun_g = Math.max(0, sun_g);
                sun_b = Math.max(0, sun_b);
                int sun_combined = (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g) | (sun_b << CLApi.bitshift_sun_b);
                ExtendedBlockStorage extendedBlockStorage = this.storageArrays[j >> 4];
                if (extendedBlockStorage != null)
                    extendedBlockStorage.setExtSkylightValue(x, j & 0xF, z, sun_combined);
            }
        }
        int n = this.heightMap[z << 4 | x];
        int i1 = i;
        int i2 = n;
        if (i2 < i1) {
            int i3 = i1;
            i1 = i2;
            i2 = i3;
        }
        if (n < this.heightMapMinimum)
            this.heightMapMinimum = n;
        if (!this.worldObj.provider.hasNoSky) {
            this.updateSkylightNeighborHeight(k - 1, m, i1, i2);
            this.updateSkylightNeighborHeight(k + 1, m, i1, i2);
            this.updateSkylightNeighborHeight(k, m - 1, i1, i2);
            this.updateSkylightNeighborHeight(k, m + 1, i1, i2);
            this.updateSkylightNeighborHeight(k, m, i1, i2);
        }
        this.isModified = true;
    }
}
