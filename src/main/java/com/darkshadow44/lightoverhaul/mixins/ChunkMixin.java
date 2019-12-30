package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkshadow44.lightoverhaul.interfaces.IChunkMixin;

import coloredlightscore.src.api.CLApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements IChunkMixin {
    @Shadow
    public World worldObj;

    @Shadow
    public ExtendedBlockStorage[] storageArrays;

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

    @Shadow
    public Block getBlock(int paramInt1, int paramInt2, int paramInt3) {
        return null;
    }

    @Shadow
    public int func_150808_b(int paramInt1, int paramInt2, int paramInt3) {
        return 0;
    }

    @Shadow
    public int getBlockMetadata(int p_76628_1_, int p_76628_2_, int p_76628_3_) {
        return 0;
    }

    @Shadow
    public void removeTileEntity(int p_150805_1_, int p_150805_2_, int p_150805_3_) {
    }

    @Shadow(remap = false)
    public TileEntity getTileEntityUnsafe(int x, int y, int z) {
        return null;
    }

    @Shadow
    public int getSavedLightValue(EnumSkyBlock p_76614_1_, int p_76614_2_, int p_76614_3_, int p_76614_4_) {
        return 0;
    }

    @Shadow
    private void propagateSkylightOcclusion(int p_76595_1_, int p_76595_2_) {
    }

    @Shadow
    public TileEntity func_150806_e(int p_150806_1_, int p_150806_2_, int p_150806_3_) {
        return null;
    }

    int[] heightMap2;
    int[][][] lightMapSun;
    int[] stainedglass_api_index;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void construct(CallbackInfo callbackInfo) {
        lightMapSun = new int[16][256][16];
        stainedglass_api_index = new int[] { 15, 14, 13, 12, 11, 10, 9, 7, 8, 6, 5, 4, 3, 2, 1, 0 };
    }

    @Override
    public boolean canReallySeeTheSky(int x, int y, int z) {
        if (((Object) this) instanceof EmptyChunk) {
            return false;
        }
        if (heightMap2 == null) {
            generateSkylightMap();
        }

        return heightMap2[z << 4 | x] <= y;
    }

    @Override
    public int getRealSunColor(int x, int y, int z) {
        return lightMapSun[x][y][z];
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
    public void generateHeightMap() {
        int i = getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                this.precipitationHeightMap[b + (b1 << 4)] = -999;
                for (int j = i + 16 - 1; j > 0; j--) {
                    if (!is_translucent_for_relightBlock(b, j - 1, b1)) {
                        this.heightMap[b1 << 4 | b] = j;
                        if (j < this.heightMapMinimum)
                            this.heightMapMinimum = j;
                        break;
                    }
                }
            }
        }
        this.isModified = true;
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public void generateSkylightMap() {
        if (heightMap2 == null) {
            heightMap2 = new int[256];
        }
        int i = this.getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                this.precipitationHeightMap[b + (b1 << 4)] = -999;
                int j;
                boolean heightMapReached = false;
                for (j = i + 16 - 1; j > 0; j--) {
                    if (!is_translucent_for_relightBlock(b, j - 1, b1) && !heightMapReached) {
                        this.heightMap[b1 << 4 | b] = j;
                        if (j < this.heightMapMinimum)
                            this.heightMapMinimum = j;
                        heightMapReached = true;
                    }
                    if (func_150808_b(b, j - 1, b1) != 0) {
                        this.heightMap2[b1 << 4 | b] = j;
                        break;
                    }
                }
                int max_y = i + 16 - 1;
                int min_y = j;
                processLightDown(b, b1, min_y, max_y);
            }
        }

        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                int j;
                boolean heightMapReached = false;
                for (j = i + 16 - 1; j > 0; j--) {
                    if (!is_translucent_for_relightBlock(b, j - 1, b1) && !heightMapReached) {
                        heightMapReached = true;
                    }
                    if (func_150808_b(b, j - 1, b1) != 0) {
                        break;
                    }
                }
                int max_y = i + 16 - 1;
                int min_y = j;

                this.updateSkylightNeighborHeight(this.xPosition * 16 + b, this.zPosition * 16 + b1, min_y, max_y);
            }
        }
        this.isModified = true;
    }

    /***
     * @author darkshadow44
     * @reason We need to handle relightBlock differently with colored light.
     */
    @Overwrite
    public boolean func_150807_a(int x, int y, int z, Block block_new, int meta_new) {
        int heightMapIndex = z << 4 | x;

        if (heightMap2 == null) {
            generateSkylightMap();
        }

        if (y >= this.precipitationHeightMap[heightMapIndex] - 1) {
            this.precipitationHeightMap[heightMapIndex] = -999;
        }

        int realHeightmapMax = this.heightMap2[heightMapIndex];
        Block block_old = this.getBlock(x, y, z);
        int meta_old = this.getBlockMetadata(x, y, z);

        if (block_old == block_new && meta_old == meta_new) {
            return false;
        } else {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];
            boolean flag = false;

            if (extendedblockstorage == null) {
                if (block_new == Blocks.air) {
                    return false;
                }

                extendedblockstorage = this.storageArrays[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, !this.worldObj.provider.hasNoSky);
                flag = y >= realHeightmapMax;
            }

            int l1 = this.xPosition * 16 + x;
            int i2 = this.zPosition * 16 + z;

            int opacity_old = block_old.getLightOpacity(this.worldObj, l1, y, i2);

            if (!this.worldObj.isRemote) {
                block_old.onBlockPreDestroy(this.worldObj, l1, y, i2, meta_old);
            }

            extendedblockstorage.func_150818_a(x, y & 15, z, block_new);
            extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta_new); // This line duplicates the one below, so breakBlock fires with valid worldstate

            if (!this.worldObj.isRemote) {
                block_old.breakBlock(this.worldObj, l1, y, i2, block_old, meta_old);
                // After breakBlock a phantom TE might have been created with incorrect meta.
                // This attempts to kill that phantom TE so the normal one can be create
                // properly later
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(block_old, getBlock(x & 0x0F, y, z & 0x0F), meta_old, getBlockMetadata(x & 0x0F, y, z & 0x0F), worldObj, l1, y, i2)) {
                    this.removeTileEntity(x & 0x0F, y, z & 0x0F);
                }
            } else if (block_old.hasTileEntity(meta_old)) {
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(block_old, block_new, meta_old, meta_new, worldObj, l1, y, i2)) {
                    this.worldObj.removeTileEntity(l1, y, i2);
                }
            }

            if (extendedblockstorage.getBlockByExtId(x, y & 15, z) != block_new) {
                return false;
            } else {
                extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta_new);

                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int opacity_new = block_new.getLightOpacity(this.worldObj, l1, y, i2);
                    boolean isColoredGlas = block_new instanceof BlockStainedGlass;

                    boolean is_addition = (opacity_new > 0) || isColoredGlas;

                    if (y >= realHeightmapMax - 1) {
                        if (is_addition) {
                            this.relightBlock(x, y + 1, z);
                        } else {
                            this.relightBlock(x, y, z);
                        }
                    }

                    if ((opacity_new != opacity_old || isColoredGlas)
                            && (opacity_new < opacity_old || this.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) > 0 || this.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 0)) {
                        this.propagateSkylightOcclusion(x, z);
                    }
                }

                TileEntity tileentity;

                if (!this.worldObj.isRemote) {
                    block_new.onBlockAdded(this.worldObj, l1, y, i2);
                }

                if (block_new.hasTileEntity(meta_new)) {
                    tileentity = this.func_150806_e(x, y, z);

                    if (tileentity != null) {
                        tileentity.updateContainingBlockInfo();
                        tileentity.blockMetadata = meta_new;
                    }
                }

                this.isModified = true;
                return true;
            }
        }
    }

    private boolean is_translucent_for_relightBlock(int x, int y, int z) {
        if (getBlock(x, y, z) instanceof BlockStainedGlass) {
            return false;
        }
        return func_150808_b(x, y, z) == 0;
    }

    private void processLightDown(int x, int z, int y_min, int y_max) {
        if (!this.worldObj.provider.hasNoSky) {
            int i3 = EnumSkyBlock.Sky.defaultLightValue;
            int sun_r = (i3 >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int sun_g = (i3 >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int sun_b = (i3 >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
            int min_opacity = 0;
            int pos = y_max;

            while (pos >= y_min) {
                int sun_combined = (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g) | (sun_b << CLApi.bitshift_sun_b);
                ExtendedBlockStorage extendedBlockStorage = this.storageArrays[pos >> 4];
                if (extendedBlockStorage != null)
                    extendedBlockStorage.setExtSkylightValue(x, pos & 0xF, z, sun_combined);
                lightMapSun[x][pos][z] = sun_combined;

                int opacity = func_150808_b(x, pos, z);
                if (opacity != 0) {
                    min_opacity = 1; // As soon as we hit something not 100% transparent, light decay starts.
                } else {
                    opacity = min_opacity;
                }

                int r_opacity = opacity;
                int g_opacity = opacity;
                int b_opacity = opacity;

                if (getBlock(x, pos, z) instanceof BlockStainedGlass) {
                    int meta = this.getBlockMetadata(x, pos, z);
                    int index = stainedglass_api_index[meta];
                    r_opacity = (int) Math.round((15 - CLApi.r[index]) / 3.0f) + 1;
                    g_opacity = (int) Math.round((15 - CLApi.g[index]) / 3.0f) + 1;
                    b_opacity = (int) Math.round((15 - CLApi.b[index]) / 3.0f) + 1;
                }

                sun_r -= r_opacity;
                sun_g -= g_opacity;
                sun_b -= b_opacity;
                sun_r = Math.max(0, sun_r);
                sun_g = Math.max(0, sun_g);
                sun_b = Math.max(0, sun_b);

                pos--;
            }
        }
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    private void relightBlock(int x, int y, int z) {
        int heightMapMax_old = this.heightMap[z << 4 | x] & 0xFF;
        int heightMapMaxReal_old = this.heightMap2[z << 4 | x] & 0xFF;
        int heightMapMax_new = heightMapMax_old;
        if (y > heightMapMax_old)
            heightMapMax_new = y;
        while (heightMapMax_new > 0 && is_translucent_for_relightBlock(x, heightMapMax_new - 1, z))
            heightMapMax_new--;

        int heightMapMaxReal_new = heightMapMax_new;
        while (heightMapMaxReal_new > 0 && func_150808_b(x, heightMapMaxReal_new - 1, z) == 0)
            heightMapMaxReal_new--;

        /*
         * We always need to recalculate, the heightmap doesn't have to change, for
         * example when stained glass is placed blow stained glass
         */
        int toprocess_max = Math.max(heightMapMax_new, heightMapMax_old);
        int toprocess_min = Math.min(heightMapMaxReal_old, heightMapMaxReal_new);

        this.heightMap2[z << 4 | x] = heightMapMaxReal_new;
        this.heightMap[z << 4 | x] = heightMapMax_new;
        int full_x = this.xPosition * 16 + x;
        int full_z = this.zPosition * 16 + z;
        this.worldObj.markBlocksDirtyVertical(x + this.xPosition * 16, z + this.zPosition * 16, toprocess_min, toprocess_max);
        processLightDown(x, z, toprocess_min, toprocess_max);
        this.updateSkylightNeighborHeight(full_x - 1, full_z, toprocess_min, toprocess_max);
        this.updateSkylightNeighborHeight(full_x + 1, full_z, toprocess_min, toprocess_max);
        this.updateSkylightNeighborHeight(full_x, full_z - 1, toprocess_min, toprocess_max);
        this.updateSkylightNeighborHeight(full_x, full_z + 1, toprocess_min, toprocess_max);
        this.updateSkylightNeighborHeight(full_x, full_z, toprocess_min, toprocess_max);

        if (toprocess_min < this.heightMapMinimum)
            this.heightMapMinimum = toprocess_min;
        this.isModified = true;
    }
}
