package lightingoverhaul.mixinmod.mixins.common.minecraft;

import lightingoverhaul.coremod.api.LightingApi;
import lightingoverhaul.mixinmod.interfaces.IChunkMixin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import scala.tools.asm.Opcodes;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements IChunkMixin  {
    @Shadow
    public World worldObj;

    @Shadow
    public ExtendedBlockStorage[] storageArrays;

    @Final
    @Shadow
    public int xPosition;

    @Final
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
    public abstract void updateSkylightNeighborHeight(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    @Shadow
    public abstract Block getBlock(int paramInt1, int paramInt2, int paramInt3);

    @Shadow
    public abstract int func_150808_b(int paramInt1, int paramInt2, int paramInt3);

    @Shadow
    public abstract int getBlockMetadata(int p_76628_1_, int p_76628_2_, int p_76628_3_);

    @Shadow
    public abstract int getSavedLightValue(EnumSkyBlock p_76614_1_, int p_76614_2_, int p_76614_3_, int p_76614_4_);

    @Shadow
    protected abstract void propagateSkylightOcclusion(int p_76595_1_, int p_76595_2_);

    @Shadow
    public abstract void generateSkylightMap();

    @Shadow
    protected abstract void relightBlock(int x, int y, int z);

    @Shadow
    public abstract TileEntity getTileEntityUnsafe(int x, int y, int z);

    @Shadow
    public abstract void removeTileEntity(int p_150805_1_, int p_150805_2_, int p_150805_3_);

    @Shadow
    public abstract TileEntity func_150806_e(int p_150806_1_, int p_150806_2_, int p_150806_3_);

    int[] heightMap2;
    int[][][] lightMapSun;
    int[] stainedglass_api_index;

    @Inject(method = "<init>*",
            at = @At("RETURN"),
            require = 1)
    private void construct(CallbackInfo callbackInfo) {
        lightMapSun = new int[16][256][16];
        stainedglass_api_index = new int[] { 15, 14, 13, 12, 11, 10, 9, 7, 8, 6, 5, 4, 3, 2, 1, 0 };
    }

    @SuppressWarnings("ConstantConditions")
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

    @Redirect(method = "getBlockLightValue",
              at = @At(value = "FIELD",
                       target = "Lnet/minecraft/world/EnumSkyBlock;defaultLightValue:I"),
              require = 2)
    private int getBlockLightValue_0(EnumSkyBlock instance) {
        return instance.defaultLightValue & 0xF;
    }

    @Redirect(method = "getBlockLightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;getExtSkylightValue(III)I"),
              require = 1)
    private int getBlockLightValue_1(ExtendedBlockStorage instance, int p_76670_1_, int p_76670_2_, int p_76670_3_) {
        return instance.getExtSkylightValue(p_76670_1_, p_76670_2_ & 15, p_76670_3_) & 0xF;
    }

    @Redirect(method = "getBlockLightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;getExtBlocklightValue(III)I"),
              require = 1)
    private int getBlockLightValue_2(ExtendedBlockStorage instance, int p_76670_1_, int p_76670_2_, int p_76670_3_) {
        return instance.getExtBlocklightValue(p_76670_1_, p_76670_2_ & 15, p_76670_3_) & 0xF;
    }

    @Inject(method = "generateSkylightMap",
            at=@At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void generateSkylightMapInjection(CallbackInfo ci) {
        ci.cancel();
        if (heightMap2 == null) {
            heightMap2 = new int[256];
        }

        int i = getTopFilledSegment();
        heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                precipitationHeightMap[b + (b1 << 4)] = -999;
                int j;
                boolean heightMapReached = false;
                for (j = i + 16 - 1; j > 0; j--) {
                    if (!is_translucent_for_relightBlock(b, j - 1, b1) && !heightMapReached) {
                        heightMap[b1 << 4 | b] = j;
                        if (j < heightMapMinimum)
                            heightMapMinimum = j;
                        heightMapReached = true;
                    }
                    if (func_150808_b(b, j - 1, b1) != 0) {
                        heightMap2[b1 << 4 | b] = j;
                        break;
                    }
                }
                if (!worldObj.provider.hasNoSky) {
                    int max_y = i + 16 - 1;

                    processLightDown(b, b1, 0, max_y);
                }
            }
        }

        if (!worldObj.provider.hasNoSky) {
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

                    updateSkylightNeighborHeight(xPosition * 16 + b, zPosition * 16 + b1, min_y, max_y);
                }
            }
        }
        this.isModified = true;
    }

    @Inject(method = "func_150807_a",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void blockChangeHandler(int x, int y, int z, Block block_new, int meta_new, CallbackInfoReturnable<Boolean> cir) {
        int heightMapIndex = z << 4 | x;

        //Added
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
            cir.setReturnValue(false);
        } else {
            ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];
            boolean flag = false;

            if (extendedblockstorage == null) {
                if (block_new == Blocks.air) {
                    cir.setReturnValue(false);
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
            extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta_new); // This line duplicates the one below, so breakBlock fires with valid world state

            if (!this.worldObj.isRemote) {
                block_old.breakBlock(this.worldObj, l1, y, i2, block_old, meta_old);
                // After breakBlock a phantom TE might have been created with incorrect meta. This attempts to kill that phantom TE so the normal one can be created properly later
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(block_old, getBlock(x & 0x0F, y, z & 0x0F), meta_old, getBlockMetadata(x & 0x0F, y, z & 0x0F), worldObj, l1, y, i2)) {
                    this.removeTileEntity(x & 0x0F, y, z & 0x0F);
                }
            } else if (block_old.hasTileEntity(meta_old))  {
                TileEntity te = this.getTileEntityUnsafe(x & 0x0F, y, z & 0x0F);
                if (te != null && te.shouldRefresh(block_old, block_new, meta_old, meta_new, worldObj, l1, y, i2)) {
                    this.worldObj.removeTileEntity(l1, y, i2);
                }
            }

            if (extendedblockstorage.getBlockByExtId(x, y & 15, z) != block_new) {
                cir.setReturnValue(false);
            } else {
                extendedblockstorage.setExtBlockMetadata(x, y & 15, z, meta_new);

                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int opacity_new = block_new.getLightOpacity(this.worldObj, l1, y, i2);

                    boolean isColoredGlass = block_new instanceof BlockStainedGlass; //Added
                    boolean is_addition = (opacity_new > 0) || isColoredGlass; //Added
                    if (y >= realHeightmapMax - 1) {
                        if (is_addition) {
                            this.relightBlock(x, y + 1, z);
                        } else {
                            this.relightBlock(x, y, z);
                        }
                    }

                    if ((opacity_new != opacity_old || isColoredGlass) //Added colored glass check
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
                cir.setReturnValue(true);
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
            int sun_r = (i3 >> LightingApi._bitshift_sun_r) & LightingApi._bitmask_sun;
            int sun_g = (i3 >> LightingApi._bitshift_sun_g) & LightingApi._bitmask_sun;
            int sun_b = (i3 >> LightingApi._bitshift_sun_b) & LightingApi._bitmask_sun;
            int min_opacity = 0;
            int pos = y_max;

            while (pos >= y_min) {
                int opacity = func_150808_b(x, pos, z);
                if (opacity != 0) {
                    min_opacity = 1; // As soon as we hit something not 100% transparent, light decay starts.
                } else {
                    opacity = min_opacity;
                }

                if (opacity == 15)
                    return;

                int r_opacity = opacity;
                int g_opacity = opacity;
                int b_opacity = opacity;

                if (getBlock(x, pos, z) instanceof BlockStainedGlass) {
                    int meta = this.getBlockMetadata(x, pos, z);
                    int index = stainedglass_api_index[meta];
                    r_opacity = Math.round((15 - LightingApi.r[index]) / 3.0f) + 1;
                    g_opacity = Math.round((15 - LightingApi.g[index]) / 3.0f) + 1;
                    b_opacity = Math.round((15 - LightingApi.b[index]) / 3.0f) + 1;
                }

                sun_r -= r_opacity;
                sun_g -= g_opacity;
                sun_b -= b_opacity;
                sun_r = Math.max(0, sun_r);
                sun_g = Math.max(0, sun_g);
                sun_b = Math.max(0, sun_b);

                int sun_combined = (sun_r << LightingApi._bitshift_sun_r) | (sun_g << LightingApi._bitshift_sun_g) | (sun_b << LightingApi._bitshift_sun_b);
                ExtendedBlockStorage extendedBlockStorage = this.storageArrays[pos >> 4];
                if (extendedBlockStorage != null) {

                    int oldLight = extendedBlockStorage.getExtSkylightValue(x, pos & 0xF, z);
                    int old_r = (oldLight >> LightingApi._bitshift_sun_r) & LightingApi._bitmask_sun;
                    int old_g = (oldLight >> LightingApi._bitshift_sun_g) & LightingApi._bitmask_sun;
                    int old_b = (oldLight >> LightingApi._bitshift_sun_b) & LightingApi._bitmask_sun;
                    int mixed_r = Math.max(old_r, sun_r);
                    int mixed_g = Math.max(old_g, sun_g);
                    int mixed_b = Math.max(old_b, sun_b);
                    int sun_combined_real = (mixed_r << LightingApi._bitshift_sun_r) | (mixed_g << LightingApi._bitshift_sun_g) | (mixed_b << LightingApi._bitshift_sun_b);

                    extendedBlockStorage.setExtSkylightValue(x, pos & 0xF, z, sun_combined_real);
                }
                lightMapSun[x][pos][z] = sun_combined;

                pos--;
            }
        }
    }

    @Inject(method = "relightBlock",
            at=@At("HEAD"),
            cancellable = true,
            require = 1)
    private void relightBlock(int x, int y, int z, CallbackInfo ci) {
        ci.cancel();
        int heightMapMax_old = this.heightMap[z << 4 | x] & 0xFF;
        int heightMapMaxReal_old = this.heightMap2[z << 4 | x] & 0xFF;
        int heightMapMax_new = Math.max(y, heightMapMax_old);
        while (heightMapMax_new > 0 && is_translucent_for_relightBlock(x, heightMapMax_new - 1, z))
            heightMapMax_new--;

        int heightMapMaxReal_new = heightMapMax_new;
        while (heightMapMaxReal_new > 0 && func_150808_b(x, heightMapMaxReal_new - 1, z) == 0)
            heightMapMaxReal_new--;

        /*
         * We always need to recalculate, the heightmap doesn't have to change, for
         * example when stained-glass is placed blow stained-glass
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

    @Redirect(method="func_150809_p",
              at=@At(value  = "FIELD",
                     target="Lnet/minecraft/world/WorldProvider;hasNoSky:Z",
                     opcode = Opcodes.GETFIELD))
    private boolean fixNoSky(WorldProvider instance) {
        return false;
    }

}
