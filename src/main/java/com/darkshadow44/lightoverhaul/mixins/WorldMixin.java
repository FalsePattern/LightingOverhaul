package com.darkshadow44.lightoverhaul.mixins;

import static coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin.CLLog;

import java.lang.reflect.InvocationTargetException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkshadow44.lightoverhaul.interfaces.IChunkMixin;

import coloredlightscore.src.api.CLApi;
import coloredlightscore.src.asm.ColoredLightsCoreDummyContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow
    public Chunk getChunkFromChunkCoords(int paramInt1, int paramInt2) {
        return null;
    }

    @Shadow
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        return false;
    }

    @Shadow
    public Block getBlock(int x, int y, int z) {
        return null;
    }

    @Shadow
    public int skylightSubtracted;

    @Shadow
    public int getBlockLightValue(int x, int y, int z) {
        return 0;
    }

    @Shadow
    public int getSavedLightValue(EnumSkyBlock paramEnumSkyBlock, int x, int y, int z) {
        return 0;
    }

    @Shadow
    public Profiler theProfiler;

    @Shadow
    public void setLightValue(EnumSkyBlock paramEnumSkyBlock, int x, int y, int z, int value) {

    }

    @Shadow
    public int getBlockMetadata(int x, int y, int z) {
        return 0;
    }

    @Shadow
    public boolean doChunksNearChunkExist(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        return false;
    }

    @Shadow
    public int getSkyBlockTypeBrightness(EnumSkyBlock paramEnumSkyBlock, int paramInt1, int paramInt2, int paramInt3) {
        return 0;
    }

    public long[] lightAdditionBlockList;
    public int[][][] lightAdditionNeeded;
    public int[] lightBackfillIndexes;
    public int[][] lightBackfillBlockList;
    public int[][][] lightBackfillNeeded;
    public int updateFlag;
    public EnumSkyBlock flagEntry;

    // white orange magenta lightblue yellow lime pink gray lightgray cyan purple
    // blue brown green red black
    int[] stainedglass_api_index;

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Inject(at = @At("RETURN"), method = { "<init>*" })
    public void init(CallbackInfo callbackInfo) {
        lightAdditionBlockList = new long[32768 * 8];
        lightAdditionNeeded = new int[64][64][64];
        lightBackfillIndexes = new int[32];
        lightBackfillBlockList = new int[32][5000 * 8];
        lightBackfillNeeded = new int[64][64][64];
        updateFlag = 1;
        flagEntry = EnumSkyBlock.Block;
        stainedglass_api_index = new int[] { 15, 14, 13, 12, 11, 10, 9, 7, 8, 6, 5, 4, 3, 2, 1, 0 };
    }

    // Copied from the world class in 1.7.2, modified from the source from 1.6.4,
    // made the method STATIC
    // Added the parameter 'World world, ' and then replaces all instances of world,
    // with WORLD
    public int getBlockLightValue_do(int x, int y, int z, boolean par4) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            if (par4 && this.getBlock(x, y, z).getUseNeighborBrightness()) {
                // heaton84 - should be world.getBlockLightValue_do,
                // switched to this.getBlockLightValue_do
                // This will save an extra invoke
                int l1 = this.getBlockLightValue_do(x, y + 1, z, false);
                int l = this.getBlockLightValue_do(x + 1, y, z, false);
                int i1 = this.getBlockLightValue_do(x - 1, y, z, false);
                int j1 = this.getBlockLightValue_do(x, y, z + 1, false);
                int k1 = this.getBlockLightValue_do(x, y, z - 1, false);

                if ((l & 0xf) > (l1 & 0xf)) {
                    l1 = l;
                }

                if ((i1 & 0xf) > (l1 & 0xf)) {
                    l1 = i1;
                }

                if ((j1 & 0xf) > (l1 & 0xf)) {
                    l1 = j1;
                }

                if ((k1 & 0xf) > (l1 & 0xf)) {
                    l1 = k1;
                }

                return l1;
            } else if (y < 0) {
                return 0;
            } else {
                if (y >= 256) {
                    y = 255;
                }

                // int cx = x >> 4;
                // int cz = z >> 4;
                Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
                x &= 0xf;
                z &= 0xf;

                // CLLog.info("NEWTEST {},{}:{}", cx, cz,
                // Integer.toBinaryString(chunk.getBlockLightValue(0, 0, 0, 15)));

                return chunk.getBlockLightValue(x, y, z, this.skylightSubtracted);
            }
        } else {
            return 15;
        }
    }

    private int calculateOpacity(int light, int opacity, Block block, int x, int y, int z) {
        int l = (light >> CLApi.bitshift_l) & 0xF;
        int r = (light >> CLApi.bitshift_r) & CLApi.bitmask;
        int g = (light >> CLApi.bitshift_g) & CLApi.bitmask;
        int b = (light >> CLApi.bitshift_b) & CLApi.bitmask;
        int sun_r = (light >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
        int sun_g = (light >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
        int sun_b = (light >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

        boolean hasColor = r > 0 || g > 0 || b > 0;

        int r_opacity = opacity;
        int g_opacity = opacity;
        int b_opacity = opacity;

        if (block instanceof BlockStainedGlass) {
            int meta = this.getBlockMetadata(x, y, z);
            int index = stainedglass_api_index[meta];
            r_opacity = (int) Math.round((15 - CLApi.r[index]) / 3.0f) + 1;
            g_opacity = (int) Math.round((15 - CLApi.g[index]) / 3.0f) + 1;
            b_opacity = (int) Math.round((15 - CLApi.b[index]) / 3.0f) + 1;
        }

        l = Math.max(0, l - opacity);
        r = Math.max(0, r - r_opacity);
        g = Math.max(0, g - g_opacity);
        b = Math.max(0, b - b_opacity);

        sun_r = Math.max(0, sun_r - r_opacity);
        sun_g = Math.max(0, sun_g - g_opacity);
        sun_b = Math.max(0, sun_b - b_opacity);

        if (hasColor && r + g + b == 0)
            l = 0;

        if (r > 15 || g > 15 || b > 15)
            l = 15;

        return (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g) | (sun_b << CLApi.bitshift_sun_b) | (l << CLApi.bitshift_l) | (r << CLApi.bitshift_r) | (g << CLApi.bitshift_g)
                | (b << CLApi.bitshift_b);
    }

    private static float getBrightness(float lightlevel) {
        float f1 = 1.0f - lightlevel / 15.0f;
        return (1.0f - f1) / (f1 * 3.0f + 1.0f);
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public float getLightBrightness(int x, int y, int z) {
        int lightlevel = this.getBlockLightValue(x, y, z);
        return getBrightness(lightlevel);
    }

    // Use this one if you want color
    /***
     * @author darkshadow44
     * @reason TODO
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        int skyBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        int light_l = (lightValue >> CLApi.bitshift_l) & 0xF;
        int light_r = (lightValue >> CLApi.bitshift_r) & CLApi.bitmask;
        int light_g = (lightValue >> CLApi.bitshift_g) & CLApi.bitmask;
        int light_b = (lightValue >> CLApi.bitshift_b) & CLApi.bitmask;

        int block_l = (blockBrightness >> CLApi.bitshift_l) & 0xF;
        int block_r = (blockBrightness >> CLApi.bitshift_r) & CLApi.bitmask;
        int block_g = (blockBrightness >> CLApi.bitshift_g) & CLApi.bitmask;
        int block_b = (blockBrightness >> CLApi.bitshift_b) & CLApi.bitmask;

        block_l = Math.max(block_l, light_l);
        block_r = Math.max(block_r, light_r);
        block_g = Math.max(block_g, light_g);
        block_b = Math.max(block_b, light_b);

        int sun_r = (skyBrightness >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
        int sun_g = (skyBrightness >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
        int sun_b = (skyBrightness >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

        return (sun_r << CLApi.bitshift_sun_r2) | (sun_g << CLApi.bitshift_sun_g2) | (sun_b << CLApi.bitshift_sun_b2) | (block_l << CLApi.bitshift_l2) | (block_r << CLApi.bitshift_r2)
                | (block_g << CLApi.bitshift_g2) | (block_b << CLApi.bitshift_b2);
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    private int computeLightValue(int par_x, int par_y, int par_z, EnumSkyBlock par1Enu) {
        Chunk chunk = getChunkFromChunkCoords(par_x >> 4, par_z >> 4);
        IChunkMixin chunkMixin = (IChunkMixin) (Object) chunk;
        if (par1Enu == EnumSkyBlock.Sky && this.canBlockSeeTheSky(par_x, par_y, par_z)) {
            return EnumSkyBlock.Sky.defaultLightValue;
        } else {
            Block block = this.getBlock(par_x, par_y, par_z);

            int currentLight = 0;
            if (par1Enu != EnumSkyBlock.Sky) {
                currentLight = (block == null ? 0 : this.getLightValueSomehow(block, (World) (Object) this, par_x, par_y, par_z));
                if ((currentLight > 0) && (currentLight <= 0xF)) {
                    currentLight = (currentLight << CLApi.bitshift_r) | (currentLight << CLApi.bitshift_g) | (currentLight << CLApi.bitshift_b) | (currentLight << CLApi.bitshift_l); // copy vanilla
                                                                                                                                                                                      // brightness
                                                                                                                                                                                      // into each
                                                                                                                                                                                      // color
                                                                                                                                                                                      // component to
                                                                                                                                                                                      // make it
                                                                                                                                                                                      // white/grey if
                                                                                                                                                                                      // it is
                                                                                                                                                                                      // uncolored.
                }
            }

            int block_l = (currentLight >> CLApi.bitshift_l) & 0xF;
            int block_r = (currentLight >> CLApi.bitshift_r) & CLApi.bitmask;
            int block_g = (currentLight >> CLApi.bitshift_g) & CLApi.bitmask;
            int block_b = (currentLight >> CLApi.bitshift_b) & CLApi.bitmask;

            int sun_r = (currentLight >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int sun_g = (currentLight >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int sun_b = (currentLight >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
            
            if (par1Enu == EnumSkyBlock.Sky && chunkMixin.canReallySeeTheSky(par_x & 0xF, par_y, par_z & 0xF))
            {
                int sun_precomputed = chunkMixin.getRealSunColor(par_x & 0xF, par_y, par_z & 0xF);
                int sun_r2 = (sun_precomputed >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                int sun_g2 = (sun_precomputed >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                int sun_b2 = (sun_precomputed >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;
                sun_r = Math.max(sun_r, sun_r2);
                sun_g = Math.max(sun_g, sun_g2);
                sun_b = Math.max(sun_b, sun_b2);
            }

            if (block_r > 15 || block_g > 15 || block_b > 15)
                block_l = 15;

            currentLight |= (block_l << CLApi.bitshift_l);

            int opacity = (block == null ? 0 : block.getLightOpacity((World) (Object) this, par_x, par_y, par_z));

            if (opacity >= 15 && currentLight > 0) {
                opacity = 1;
            }

            if (opacity < 1) {
                opacity = 1;
            }

            if (opacity >= 15) {
                return 0;
            } else {

                for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
                    int l1 = par_x + Facing.offsetsXForSide[faceIndex];
                    int i2 = par_y + Facing.offsetsYForSide[faceIndex];
                    int j2 = par_z + Facing.offsetsZForSide[faceIndex];
                    
                    if (faceIndex == 1)
                    {
                        if (getBlock(l1, i2, j2) instanceof BlockStainedGlass)
                        {
                            if (par1Enu == EnumSkyBlock.Sky && chunkMixin.canReallySeeTheSky(l1 & 0xF, i2, j2 & 0xF))
                                continue;
                        }
                    }

                    int neighborLight = this.getSavedLightValue(par1Enu, l1, i2, j2);

                    int light = calculateOpacity(neighborLight, opacity, block, par_x, par_y, par_z);

                    int block_l2 = (light >> CLApi.bitshift_l) & 0xF;
                    int block_r2 = (light >> CLApi.bitshift_r) & CLApi.bitmask;
                    int block_g2 = (light >> CLApi.bitshift_g) & CLApi.bitmask;
                    int block_b2 = (light >> CLApi.bitshift_b) & CLApi.bitmask;

                    int sun_r2 = (light >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                    int sun_g2 = (light >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                    int sun_b2 = (light >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                    block_l = Math.max(block_l, block_l2);
                    block_r = Math.max(block_r, block_r2);
                    block_g = Math.max(block_g, block_g2);
                    block_b = Math.max(block_b, block_b2);

                    sun_r = Math.max(sun_r, sun_r2);
                    sun_g = Math.max(sun_g, sun_g2);
                    sun_b = Math.max(sun_b, sun_b2);
                }

                return (sun_r << CLApi.bitshift_sun_r) | (sun_g << CLApi.bitshift_sun_g) | (sun_b << CLApi.bitshift_sun_b) | (block_l << CLApi.bitshift_l) | (block_r << CLApi.bitshift_r)
                        | (block_g << CLApi.bitshift_g) | (block_b << CLApi.bitshift_b);
            }
        }
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public boolean updateLightByType(EnumSkyBlock par1Enu, int par_x, int par_y, int par_z) {
        return this.updateLightByType_withIncrement(par1Enu, par_x, par_y, par_z, true, par_x, par_y, par_z);
    }

    private String makeLightStr(int block_r, int block_g, int block_b, int sun_r, int sun_g, int sun_b) {
        return "(block_r : " + block_r + ", block_g: " + block_g + ", block_b: " + block_b + ", sun_r: " + sun_r + ", sun_g: " + sun_g + ", sun_b: " + sun_b + ")";
    }

    private String makePosStr(int x, int y, int z) {
        return "at (X: " + x + ", Y: " + y + " Z: " + z + ")";
    }

    public boolean updateLightByType_withIncrement(EnumSkyBlock par1Enu, int par_x, int par_y, int par_z, boolean shouldIncrement, int rel_x, int rel_y, int rel_z) {
        if (!this.doChunksNearChunkExist(par_x, par_y, par_z, 17)) {
            return false;
        } else {
            if (shouldIncrement) {
                // Increment the updateFlag ONLY on a fresh call... This keeps the updateFlag
                // consistent when the algorithm recurses
                // if ((flag_entry != updateFlag) && (flag_entry != updateFlag+1)) { // Light
                // has not been visited by the algorithm yet
                // if (flag_entry == updateFlag) { // Light has been marked for a later update
                // if (flag_entry == updateFlag+1) { // Light has been visited and processed,
                // don't visit in the future generations of this algorithm
                this.updateFlag += 2;
                this.flagEntry = par1Enu;
            }

            this.theProfiler.startSection("getBrightness");

            int lightAdditionsSatisfied = 0;
            int lightAdditionsCalled = 0;
            int filler = 0;
            int getter = 0;
            int lightEntry;

            long savedLightValue = this.getSavedLightValue(par1Enu, par_x, par_y, par_z);
            long compLightValue = this.computeLightValue(par_x, par_y, par_z, par1Enu);
            long queueEntry;
            int queue_x;
            int queue_y;
            int queue_z;
            int queueLightEntry;

            int man_x;
            int man_y;
            int man_z;
            long manhattan_distance;

            int opacity;

            int neighbor_x;
            int neighbor_y;
            int neighbor_z;

            int neighborIndex;
            int neighborLightEntry;
            final boolean DEBUG = false;

            this.theProfiler.endStartSection("lightAddition");

            int saved_block_r = (int) (savedLightValue >> CLApi.bitshift_r) & CLApi.bitmask;
            int saved_block_g = (int) (savedLightValue >> CLApi.bitshift_g) & CLApi.bitmask;
            int saved_block_b = (int) (savedLightValue >> CLApi.bitshift_b) & CLApi.bitmask;
            int saved_sun_r = (int) (savedLightValue >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int saved_sun_g = (int) (savedLightValue >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int saved_sun_b = (int) (savedLightValue >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

            int comp_block_r = (int) (compLightValue >> CLApi.bitshift_r) & CLApi.bitmask;
            int comp_block_g = (int) (compLightValue >> CLApi.bitshift_g) & CLApi.bitmask;
            int comp_block_b = (int) (compLightValue >> CLApi.bitshift_b) & CLApi.bitmask;
            int comp_sun_r = (int) (compLightValue >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
            int comp_sun_g = (int) (compLightValue >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
            int comp_sun_b = (int) (compLightValue >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

            final int offset = 31; // Offset for the start block
            final int size = 64; // Number or blocks in one directon
            final int coord_size = 7; // Number of bits for one axis
            final int coord_mask = (1 << coord_size) - 1; // Mask for getting one axis
            final int startCoordOne = (1 << coord_size) / 2;
            final long startCoord = (startCoordOne) | (startCoordOne << coord_size) | (startCoordOne << (coord_size * 2));

            // Format of lightAdditionBlockList word:
            // bbbbb.ggggg.rrrrr.LLLLzzzzzzzyyyyyyyxxxxxxx
            // x/y/z are relative offsets
            if (comp_block_r > saved_block_r || comp_block_g > saved_block_g || comp_block_b > saved_block_b || comp_sun_r > saved_sun_r || comp_sun_g > saved_sun_g || comp_sun_b > saved_sun_b) { // compLightValue
                                                                                                                                                                                                    // has
                                                                                                                                                                                                    // components
                                                                                                                                                                                                    // that
                                                                                                                                                                                                    // are
                                                                                                                                                                                                    // larger
                                                                                                                                                                                                    // than
                                                                                                                                                                                                    // savedLightValue,
                                                                                                                                                                                                    // the
                                                                                                                                                                                                    // block
                                                                                                                                                                                                    // at
                                                                                                                                                                                                    // the
                                                                                                                                                                                                    // current
                                                                                                                                                                                                    // position
                                                                                                                                                                                                    // is
                                                                                                                                                                                                    // brighter
                                                                                                                                                                                                    // than
                                                                                                                                                                                                    // the
                                                                                                                                                                                                    // saved
                                                                                                                                                                                                    // value
                                                                                                                                                                                                    // at
                                                                                                                                                                                                    // the
                                                                                                                                                                                                    // current
                                                                                                                                                                                                    // positon...
                                                                                                                                                                                                    // it
                                                                                                                                                                                                    // must
                                                                                                                                                                                                    // have
                                                                                                                                                                                                    // been
                                                                                                                                                                                                    // made
                                                                                                                                                                                                    // brighter
                                                                                                                                                                                                    // somehow
                // Light Splat/Spread

                this.lightAdditionNeeded[offset][offset][offset] = this.updateFlag; // Light needs
                                                                                    // processing processed
                lightAdditionsCalled++;
                if (DEBUG) {
                    CLLog.warn(
                            "Spread Addition Original " + makePosStr(par_x, par_y, par_z) + " with comp " + makeLightStr(comp_block_r, comp_block_g, comp_block_b, comp_sun_r, comp_sun_g, comp_sun_b)
                                    + " and saved " + makeLightStr(saved_block_r, saved_block_g, saved_block_b, comp_sun_r, comp_sun_g, comp_sun_b));
                }
                this.lightAdditionBlockList[getter++] = startCoord | (compLightValue << (coord_size * 3));

                while (filler < getter) {
                    queueEntry = this.lightAdditionBlockList[filler++]; // Get Entry at l, which starts at 0
                    queue_x = ((int) (queueEntry & coord_mask) - size + par_x); // Get Entry X coord
                    queue_y = ((int) ((queueEntry >> coord_size) & coord_mask) - size + par_y); // Get Entry Y coord
                    queue_z = ((int) ((queueEntry >> (coord_size * 2)) & coord_mask) - size + par_z); // Get Entry Z
                                                                                                      // coord

                    if (this.lightAdditionNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] == this.updateFlag) { // Light has been marked for a later update

                        queueLightEntry = (int) (queueEntry >>> (coord_size * 3)); // Get Entry's saved Light
                        neighborLightEntry = this.getSavedLightValue(par1Enu, queue_x, queue_y, queue_z); // Getthe savedLight Level at the entry's location - Instead of comparing
                                                                                                          // against the value saved on disk every iteration, and checking to see if it's
                                                                                                          // been updated already... Consider storing values in a temp 3D array as they
                                                                                                          // are gathered and applying changes all at once

                        if (Math.abs(queue_x - rel_x) < offset && Math.abs(queue_y - rel_y) < offset && Math.abs(queue_z - rel_z) < offset) {
                            this.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = this.updateFlag + 1; // Light has been visited and
                                                                                                                                                          // processed
                        }
                        this.lightAdditionNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag + 1; // Light has been visited and processed

                        lightAdditionsSatisfied++;

                        int queue_block_r = (queueLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                        int queue_block_g = (queueLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                        int queue_block_b = (queueLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;
                        int queue_sun_r = (queueLightEntry >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                        int queue_sun_g = (queueLightEntry >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                        int queue_sun_b = (queueLightEntry >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                        int edge_block_r = (int) (neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                        int edge_block_g = (int) (neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                        int edge_block_b = (int) (neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;
                        int edge_sun_r = (int) (neighborLightEntry >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                        int edge_sun_g = (int) (neighborLightEntry >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                        int edge_sun_b = (int) (neighborLightEntry >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                        if (queue_block_r > edge_block_r || queue_block_g > edge_block_g || queue_block_b > edge_block_b || queue_sun_r > edge_sun_r || queue_sun_g > edge_sun_g
                                || queue_sun_b > edge_sun_b) { // Components in queueLightEntry are brighter than in
                                                               // edgeLightEntry
                            man_x = MathHelper.abs_int(queue_x - par_x);
                            man_y = MathHelper.abs_int(queue_y - par_y);
                            man_z = MathHelper.abs_int(queue_z - par_z);
                            manhattan_distance = man_x + man_y + man_z;

                            this.setLightValue(par1Enu, queue_x, queue_y, queue_z, queueLightEntry);
                            if (DEBUG) {
                                CLLog.warn("Spread " + makePosStr(queue_x, queue_y, queue_z) + " with queue "
                                        + makeLightStr(queue_block_r, queue_block_g, queue_block_b, queue_sun_r, queue_sun_g, queue_sun_b) + " and edge "
                                        + makeLightStr(edge_block_r, edge_block_g, edge_block_b, edge_sun_r, edge_sun_g, edge_sun_b));
                            }
                            int limit_test = Math.max(Math.max(comp_block_r, comp_block_g), comp_block_b);
                            limit_test = Math.max(Math.max(Math.max(limit_test, comp_sun_r), comp_sun_g), comp_sun_b);

                            // if ((manhattan_distance < ((compLightValue & 0x0000F) - 1)) || (par1Enu ==
                            // EnumSkyBlock.Sky && (man_x<14) && (man_y<14) && (man_z<14))) { //Limits the
                            // splat size to the initial brightness value, skylight checks bypass this, as
                            // they aren't always diamond-shaped
                            if (manhattan_distance < limit_test - 1) {
                                for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                                    neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                                    neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                                    neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                                    if (neighbor_y < 0 || neighbor_y > 255)
                                        continue;

                                    lightEntry = this.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset];
                                    if (lightEntry != this.updateFlag && (lightEntry != this.updateFlag + 1 || !shouldIncrement)) { // on
                                                                                                                                    // recursive
                                                                                                                                    // calls,
                                                                                                                                    // ignore
                                                                                                                                    // instances
                                                                                                                                    // of
                                                                                                                                    // this.updateFlag
                                                                                                                                    // being
                                                                                                                                    // flag
                                                                                                                                    // + 1

                                        Block neighborBlock = this.getBlock(neighbor_x, neighbor_y, neighbor_z);
                                        opacity = Math.max(1, neighborBlock.getLightOpacity((World) (Object) this, neighbor_x, neighbor_y, neighbor_z));

                                        // Proceed only if the block is non-solid
                                        if (opacity < 15) {

                                            // Get Saved light value from face
                                            neighborLightEntry = this.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);

                                            int queueLightEntryFiltered = calculateOpacity(queueLightEntry, opacity, neighborBlock, neighbor_x, neighbor_y, neighbor_z);

                                            int queue_filtered_block_r = (queueLightEntryFiltered >> CLApi.bitshift_r) & CLApi.bitmask;
                                            int queue_filtered_block_g = (queueLightEntryFiltered >> CLApi.bitshift_g) & CLApi.bitmask;
                                            int queue_filtered_block_b = (queueLightEntryFiltered >> CLApi.bitshift_b) & CLApi.bitmask;
                                            int queue_filtered_sun_r = (queueLightEntryFiltered >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                                            int queue_filtered_sun_g = (queueLightEntryFiltered >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                                            int queue_filtered_sun_b = (queueLightEntryFiltered >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                                            int neighbor_block_r = (neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                            int neighbor_block_g = (neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                            int neighbor_block_b = (neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;
                                            int neighbor_sun_r = (neighborLightEntry >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                                            int neighbor_sun_g = (neighborLightEntry >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                                            int neighbor_sun_b = (neighborLightEntry >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                                            int final_block_r = queue_block_r > neighbor_block_r ? Math.max(0, queue_filtered_block_r) : neighbor_block_r;
                                            int final_block_g = queue_block_g > neighbor_block_g ? Math.max(0, queue_filtered_block_g) : neighbor_block_g;
                                            int final_block_b = queue_block_b > neighbor_block_b ? Math.max(0, queue_filtered_block_b) : neighbor_block_b;
                                            int final_sun_r = queue_sun_r > neighbor_sun_r ? Math.max(0, queue_filtered_sun_r) : neighbor_sun_r;
                                            int final_sun_g = queue_sun_g > neighbor_sun_g ? Math.max(0, queue_filtered_sun_g) : neighbor_sun_g;
                                            int final_sun_b = queue_sun_b > neighbor_sun_b ? Math.max(0, queue_filtered_sun_b) : neighbor_sun_b;

                                            long light_combine = (final_block_r << CLApi.bitshift_r) | (final_block_g << CLApi.bitshift_g) | (final_block_b << CLApi.bitshift_b)
                                                    | (final_sun_r << CLApi.bitshift_sun_r) | (final_sun_g << CLApi.bitshift_sun_g) | (final_sun_b << CLApi.bitshift_sun_b);

                                            if (((final_block_r > neighbor_block_r) || (final_block_g > neighbor_block_g) || (final_block_b > neighbor_block_b) || (final_sun_r > neighbor_sun_r)
                                                    || (final_sun_g > neighbor_sun_g) || (final_sun_b > neighbor_sun_b)) && (getter < this.lightAdditionBlockList.length)) {
                                                this.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = this.updateFlag; // Mark
                                                                                                                                                                                   // neighbor
                                                                                                                                                                                   // to
                                                                                                                                                                                   // be
                                                                                                                                                                                   // processed
                                                this.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size)
                                                        | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | (light_combine << (coord_size * 3));
                                                lightAdditionsCalled++;
                                            } else if ((queue_block_r + opacity < neighbor_block_r) || (queue_block_g + opacity < neighbor_block_g) || (queue_block_b + opacity < neighbor_block_b)
                                                    || (queue_sun_r + opacity < neighbor_sun_r) || (queue_sun_g + opacity < neighbor_sun_g) || (queue_sun_b + opacity < neighbor_sun_b)) {
                                                if (Math.abs(queue_x - rel_x) < offset && Math.abs(queue_y - rel_y) < offset && Math.abs(queue_z - rel_z) < offset) {
                                                    this.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = this.updateFlag; // Mark
                                                                                                                                                                              // queue
                                                                                                                                                                              // location
                                                                                                                                                                              // to
                                                                                                                                                                              // be
                                                                                                                                                                              // re-processed
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if ((filler > 24389 * 2) || (lightAdditionsCalled != lightAdditionsSatisfied)) {
                CLLog.warn("Error in Light Addition:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue) + " Comp:"
                        + Integer.toBinaryString((int) compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
                        + lightAdditionsSatisfied);
            }

            if (shouldIncrement) { // Only proceed if we are NOT in a recursive call
                this.theProfiler.endStartSection("lightSubtraction");

                // Reset indexes
                filler = 0;
                getter = 0;

                if (saved_block_r > comp_block_r || saved_block_g > comp_block_g || saved_block_b > comp_block_b || saved_sun_r > comp_sun_r || saved_sun_g > comp_sun_g || saved_sun_b > comp_sun_b) { // savedLightValue
                                                                                                                                                                                                        // has
                                                                                                                                                                                                        // components
                                                                                                                                                                                                        // that
                                                                                                                                                                                                        // are
                                                                                                                                                                                                        // larger
                                                                                                                                                                                                        // than
                                                                                                                                                                                                        // compLightValue
                    // Light Destruction

                    this.setLightValue(par1Enu, par_x, par_y, par_z, (int) compLightValue); // This kills the
                                                                                            // light
                    if (DEBUG) {
                        CLLog.warn("Destruction1 " + makePosStr(par_x, par_y, par_z) + " with saved " + makeLightStr(saved_block_r, saved_block_g, saved_block_b, saved_sun_r, saved_sun_g, saved_sun_b)
                                + " and comp " + makeLightStr(comp_block_r, comp_block_g, comp_block_b, comp_sun_r, comp_sun_g, comp_sun_b));
                    }
                    this.lightAdditionBlockList[getter++] = (startCoord | (savedLightValue << (coord_size * 3)));

                    while (filler <= getter) {
                        queueEntry = this.lightAdditionBlockList[filler++]; // Get Entry at l, which starts at 0
                        queue_x = ((int) (queueEntry & coord_mask) - size + par_x); // Get Entry X coord
                        queue_y = ((int) ((queueEntry >> coord_size) & coord_mask) - size + par_y); // Get Entry Y coord
                        queue_z = ((int) ((queueEntry >> (coord_size * 2)) & coord_mask) - size + par_z); // Get Entry Z
                                                                                                          // coord
                        queueLightEntry = (int) (queueEntry >>> (coord_size * 3)); // Get Entry's saved Light

                        man_x = MathHelper.abs_int(queue_x - par_x);
                        man_y = MathHelper.abs_int(queue_y - par_y);
                        man_z = MathHelper.abs_int(queue_z - par_z);
                        manhattan_distance = man_x + man_y + man_z;

                        int limit_test = Math.max(Math.max(saved_block_r, saved_block_g), saved_block_b);
                        limit_test = Math.max(Math.max(Math.max(limit_test, saved_sun_r), saved_sun_g), saved_sun_b);

                        if (manhattan_distance < limit_test) { // Limits the splat size to the initial brightness value
                            for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                                neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                                neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                                neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                                if (neighbor_y < 0 || neighbor_y > 255)
                                    continue;

                                man_x = MathHelper.abs_int(neighbor_x - par_x);
                                man_y = MathHelper.abs_int(neighbor_y - par_y);
                                man_z = MathHelper.abs_int(neighbor_z - par_z);

                                opacity = Math.max(1, this.getBlock(neighbor_x, neighbor_y, neighbor_z).getLightOpacity((World) (Object) this, neighbor_x, neighbor_y, neighbor_z));
                                neighborLightEntry = this.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);
                                neighborLightEntry = neighborLightEntry & ~0xF;

                                int queue_block_r = (queueLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                int queue_block_g = (queueLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                int queue_block_b = (queueLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;
                                int queue_sun_r = (queueLightEntry >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                                int queue_sun_g = (queueLightEntry >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                                int queue_sun_b = (queueLightEntry >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                                int neighbor_block_r = (neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                int neighbor_block_g = (neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                int neighbor_block_b = (neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;
                                int neighbor_sun_r = (neighborLightEntry >> CLApi.bitshift_sun_r) & CLApi.bitmask_sun;
                                int neighbor_sun_g = (neighborLightEntry >> CLApi.bitshift_sun_g) & CLApi.bitmask_sun;
                                int neighbor_sun_b = (neighborLightEntry >> CLApi.bitshift_sun_b) & CLApi.bitmask_sun;

                                if (opacity < 15 || neighborLightEntry > 0) {
                                    // Get Saved light value from face

                                    // |------------------maximum theoretical light value------------------|
                                    // |------saved light value------|
                                    int final_block_r = (Math.max(queue_block_r - (man_x + man_y + man_z), 0) >= neighbor_block_r) ? 0 : neighbor_block_r;
                                    int final_block_g = (Math.max(queue_block_g - (man_x + man_y + man_z), 0) >= neighbor_block_g) ? 0 : neighbor_block_g;
                                    int final_block_b = (Math.max(queue_block_b - (man_x + man_y + man_z), 0) >= neighbor_block_b) ? 0 : neighbor_block_b;
                                    int final_sun_r = (Math.max(queue_sun_r - (man_x + man_y + man_z), 0) >= neighbor_sun_r) ? 0 : neighbor_sun_r;
                                    int final_sun_g = (Math.max(queue_sun_g - (man_x + man_y + man_z), 0) >= neighbor_sun_g) ? 0 : neighbor_sun_g;
                                    int final_sun_b = (Math.max(queue_sun_b - (man_x + man_y + man_z), 0) >= neighbor_sun_b) ? 0 : neighbor_sun_b;

                                    int sortValue = 0;
                                    if ((queue_block_r > 0) && (final_block_r > sortValue)) {
                                        sortValue = (int) final_block_r;
                                    }
                                    if ((queue_block_g > 0) && (final_block_g > sortValue)) {
                                        sortValue = (int) final_block_g;
                                    }
                                    if ((queue_block_b > 0) && (final_block_b > sortValue)) {
                                        sortValue = (int) final_block_b;
                                    }

                                    if ((queue_sun_r > 0) && (final_sun_r > sortValue)) {
                                        sortValue = (int) final_sun_r;
                                    }
                                    if ((queue_sun_g > 0) && (final_sun_g > sortValue)) {
                                        sortValue = (int) final_sun_g;
                                    }
                                    if ((queue_sun_b > 0) && (final_sun_b > sortValue)) {
                                        sortValue = (int) final_sun_b;
                                    }

                                    long light_combine = (final_block_r << CLApi.bitshift_r) | (final_block_g << CLApi.bitshift_g) | (final_block_b << CLApi.bitshift_b)
                                            | (final_sun_r << CLApi.bitshift_sun_r) | (final_sun_g << CLApi.bitshift_sun_g) | (final_sun_b << CLApi.bitshift_sun_b);
                                    // If the light we are looking at on the edge is brighter or equal to the
                                    // current light in any way, then there must be a light over there that's doing
                                    // it, so we'll stop eating colors and lights in that direction
                                    if (neighborLightEntry != light_combine) {

                                        if (sortValue != 0) {
                                            if (final_block_r == sortValue) {
                                                final_block_r = 0;
                                            }
                                            if (final_block_g == sortValue) {
                                                final_block_g = 0;
                                            }
                                            if (final_block_b == sortValue) {
                                                final_block_b = 0;
                                            }
                                            if (final_sun_r == sortValue) {
                                                final_sun_r = 0;
                                            }
                                            if (final_sun_g == sortValue) {
                                                final_sun_g = 0;
                                            }
                                            if (final_sun_b == sortValue) {
                                                final_sun_b = 0;
                                            }
                                            light_combine = (final_block_r << CLApi.bitshift_r) | (final_block_g << CLApi.bitshift_g) | (final_block_b << CLApi.bitshift_b)
                                                    | (final_sun_r << CLApi.bitshift_sun_r) | (final_sun_g << CLApi.bitshift_sun_g) | (final_sun_b << CLApi.bitshift_sun_b);

                                            this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag;
                                            this.lightBackfillBlockList[sortValue - 1][this.lightBackfillIndexes[sortValue - 1]++] = (neighbor_x - par_x + size)
                                                    | ((neighbor_y - par_y + size) << (coord_size * 1)) | ((neighbor_z - par_z + size) << (coord_size * 2)); // record
                                                                                                                                                             // coordinates
                                                                                                                                                             // for
                                                                                                                                                             // backfill
                                        }
                                        this.setLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z, (int) light_combine); // This kills the light
                                        if (DEBUG) {
                                            CLLog.warn("Destruction2 at (X: " + neighbor_x + ", Y: " + neighbor_y + ", Z: " + neighbor_z + ") with (block_r: " + final_block_r + ", block_g: "
                                                    + final_block_g + ", block_b: " + final_block_b + ", sun_r: " + final_sun_r + ", sun_g: " + final_sun_g + ", sun_b: " + final_sun_b + ")");
                                        }
                                        this.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size)
                                                | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | ((long) queueLightEntry << (coord_size * 3)); // this array keeps the
                                                                                                                                                                  // algorithm going,
                                                                                                                                                                  // don't touch
                                    } else {
                                        if (sortValue != 0) {
                                            this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag;
                                            this.lightBackfillBlockList[sortValue - 1][this.lightBackfillIndexes[sortValue - 1]++] = (queue_x - par_x + size) | ((queue_y - par_y + size) << coord_size)
                                                    | ((queue_z - par_z + size) << (coord_size * 2)); // record
                                                                                                      // coordinates
                                                                                                      // for
                                                                                                      // backfill
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (filler > 4097 * 2) {
                        CLLog.warn("Light Subtraction Overfilled:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue)
                                + " Comp:" + Integer.toBinaryString((int) compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
                                + lightAdditionsSatisfied);
                    }

                    this.theProfiler.endStartSection("lightBackfill");

                    // Backfill
                    for (filler = this.lightBackfillIndexes.length - 1; filler >= 0; filler--) {
                        while (this.lightBackfillIndexes[filler] > 0) {
                            getter = this.lightBackfillBlockList[filler][--this.lightBackfillIndexes[filler]];
                            queue_x = (getter & coord_mask) - size + par_x; // Get Entry X coord
                            queue_y = (getter >> (coord_size * 1) & coord_mask) - size + par_y; // Get Entry Y coord
                            queue_z = (getter >> (coord_size * 2) & coord_mask) - size + par_z; // Get Entry Z coord

                            if (this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] == this.updateFlag) {
                                this.updateLightByType_withIncrement(par1Enu, queue_x, queue_y, queue_z, false, rel_x, rel_y, rel_z); /// oooooOOOOoooo spoooky!
                            }
                        }
                    }
                }
            }

            this.theProfiler.endSection();
            return true;
        }
    }

    /**
     * Patching in Dynamic Lights Compatibility
     */
    private int getLightValueSomehow(Block block, World world, int par_x, int par_y, int par_z) {
        if (ColoredLightsCoreDummyContainer.getDynamicLight != null && world.isRemote) {
            try {
                int a = (Integer) ColoredLightsCoreDummyContainer.getDynamicLight.invoke(null, world, block, par_x, par_y, par_z);
                if (a != 0)
                    CLLog.info("got :" + a);
                return a;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return block.getLightValue(world, par_x, par_y, par_z);
    }
}
