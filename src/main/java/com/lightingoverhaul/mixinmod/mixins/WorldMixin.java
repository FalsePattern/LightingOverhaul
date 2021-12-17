package com.lightingoverhaul.mixinmod.mixins;

import java.lang.reflect.InvocationTargetException;

import com.lightingoverhaul.coremod.util.Light;
import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.coremod.util.LightBuffer;
import com.lightingoverhaul.coremod.util.RGB;
import com.lightingoverhaul.coremod.util.operators.BinaryOperator;
import com.lightingoverhaul.coremod.asm.CoreDummyContainer;
import com.lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import com.lightingoverhaul.mixinmod.helper.RGBHelper;
import com.lightingoverhaul.mixinmod.interfaces.IChunkMixin;
import lombok.Cleanup;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
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

    @Final
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
    @SideOnly(Side.CLIENT)
    public int getSkyBlockTypeBrightness(EnumSkyBlock paramEnumSkyBlock, int paramInt1, int paramInt2, int paramInt3) {
        return 0;
    }

    @Shadow protected abstract int computeLightValue(int p_98179_1_, int p_98179_2_, int p_98179_3_, EnumSkyBlock p_98179_4_);

    public long[] lightAdditionBlockList;
    public int[][][] lightAdditionNeeded;
    public int[][][] lightAdditionNeededOpacity;
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
        lightAdditionNeededOpacity = new int[64][64][64];
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

    private int calculateOpacity(int lightIn, int opacityIn, Block block, int x, int y, int z) {
        @Cleanup Light light = LightBuffer.getLight();
        @Cleanup RGB opacity = LightBuffer.getRGB();
        RGB color = light.color;
        RGB sun = light.sun;

        light.fromLight(lightIn);

        boolean hasColor = color.r > 0 || color.g > 0 || color.b > 0;

        opacity.set(opacityIn);

        if (block instanceof BlockStainedGlass) {
            int meta = this.getBlockMetadata(x, y, z);
            int index = stainedglass_api_index[meta];

            opacity.apply(LightingApi.colors[index], (a) -> Math.round((15 - a) / 3.0F) + 1);
        }
        BinaryOperator oper = (a, b) -> Math.max(0, a - b);
        light.l = oper.apply(light.l, opacityIn);
        color.applySelf(opacity, oper);
        sun.applySelf(opacity, oper);

        if (hasColor && color.r + color.g + color.b == 0)
            light.l = 0;

        if (color.r > 15 || color.g > 15 || color.b > 15)
            light.l = 15;

        return light.toLight();
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "getLightBrightnessForSkyBlocks",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    public void getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue, CallbackInfoReturnable<Integer> cir) {
        int skyBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
        cir.setReturnValue(RGBHelper.computeLightBrightnessForSkyBlocks(skyBrightness, blockBrightness, lightValue));
    }

    @Inject(method = "computeLightValue",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void computeLightValueReplacement(int x, int y, int z, EnumSkyBlock par1Enu, CallbackInfoReturnable<Integer> cir) {
        Chunk chunk = getChunkFromChunkCoords(x >> 4, z >> 4);
        IChunkMixin chunkMixin = (IChunkMixin) chunk;
        if (par1Enu == EnumSkyBlock.Sky && this.canBlockSeeTheSky(x, y, z)) {
            cir.setReturnValue(EnumSkyBlock.Sky.defaultLightValue);
        } else {
            Block block = this.getBlock(x, y, z);

            int currentLight = 0;
            if (par1Enu != EnumSkyBlock.Sky) {
                currentLight = (block == null ? 0 : this.getLightValueSomehow(block, (World) (Object) this, x, y, z));
                if ((currentLight > 0) && (currentLight <= 0xF)) {
                    // copy vanilla brightness into each color component to make it white/grey if it is uncolored.
                    currentLight = (currentLight << LightingApi._bitshift_r) | (currentLight << LightingApi._bitshift_g) | (currentLight << LightingApi._bitshift_b) | (currentLight << LightingApi._bitshift_l);
                }
            }

            @Cleanup Light current = LightBuffer.getLight();
            current.fromLight(currentLight);

            if (par1Enu == EnumSkyBlock.Sky && chunkMixin.canReallySeeTheSky(x & 0xF, y, z & 0xF)) {
                int sun_precomputed = chunkMixin.getRealSunColor(x & 0xF, y, z & 0xF);
                @Cleanup RGB sun2 = LightBuffer.getRGB();
                sun2.apply(LightingApi.bitshift_sun, (shift) -> (sun_precomputed >> shift) & LightingApi._bitmask_sun);

                current.sun.applySelf(sun2, Math::max);
            }

            if (current.color.r > 15 || current.color.g > 15 || current.color.b > 15)
                current.l = 15;

            currentLight |= (current.l << LightingApi._bitshift_l);

            int opacity = (block == null ? 0 : block.getLightOpacity((World) (Object) this, x, y, z));

            if (opacity >= 15 && currentLight > 0) {
                opacity = 1;
            }

            if (opacity < 1) {
                opacity = 1;
            }

            if (opacity >= 15) {
                cir.setReturnValue(0);
            } else {

                for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
                    int l1 = x + Facing.offsetsXForSide[faceIndex];
                    int i2 = y + Facing.offsetsYForSide[faceIndex];
                    int j2 = z + Facing.offsetsZForSide[faceIndex];

                    if (faceIndex == 1) {
                        if (getBlock(l1, i2, j2) instanceof BlockStainedGlass) {
                            if (par1Enu == EnumSkyBlock.Sky && chunkMixin.canReallySeeTheSky(l1 & 0xF, i2, j2 & 0xF))
                                continue;
                        }
                    }

                    int neighborLight = this.getSavedLightValue(par1Enu, l1, i2, j2);

                    int light = calculateOpacity(neighborLight, opacity, block, x, y, z);

                    @Cleanup Light neighbor = LightBuffer.getLight();
                    neighbor.fromLight(light);
                    current.applySelf(neighbor, Math::max);
                }
                cir.setReturnValue(current.toLight());
            }
        }
    }

    private EnumSkyBlock ulbt_par1Enu;
    private int ulbt_retval = 0;

    @ModifyVariable(method = "updateLightByType",
                    at = @At(value = "HEAD",
                       ordinal = 0),
                    require = 1,
                    argsOnly = true)
    private EnumSkyBlock updateLightByTypeGetPar1Enu(EnumSkyBlock par1Enu) {
        ulbt_par1Enu = par1Enu;
        return par1Enu;
    }

    @Redirect(method = "updateLightByType",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/World;doChunksNearChunkExist(IIII)Z",
                       ordinal = 0),
              require = 1)
    private boolean updateLightByTypeReplacement(World instance, int par_x, int par_y, int par_z, int p_72873_4_) {
        ulbt_retval = this.updateLightByType_withIncrement(ulbt_par1Enu, par_x, par_y, par_z, true, par_x, par_y, par_z) ? 1 : 0;
        return false;
    }

    @ModifyConstant(method = "updateLightByType",
                    constant = @Constant(intValue = 0,
                                         expandZeroConditions = Constant.Condition.LESS_THAN_ZERO,
                                         ordinal = 0),
                    require = 1)
    private int updateLightByTypeReturn(int constant) {
        return ulbt_retval;
    }

    private String makeLightStr(Light light) {
        return "(block_r : " + light.color.r + ", block_g: " + light.color.r + ", block_b: " + light.color.b + ", sun_r: " + light.sun.r + ", sun_g: " + light.sun.g + ", sun_b: " + light.sun.b + ")";
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

            @Cleanup Light tmp = LightBuffer.getLight();

            @Cleanup Light saved = LightBuffer.getLight();

            saved.fromLight((int) savedLightValue);

            @Cleanup Light comp = LightBuffer.getLight();

            comp.fromLight((int) compLightValue);

            final int offset = 31; // Offset for the start block
            final int size = 64; // Number or blocks in one directon
            final int coord_size = 7; // Number of bits for one axis
            final int coord_mask = (1 << coord_size) - 1; // Mask for getting one axis
            final int startCoordOne = (1 << coord_size) / 2;
            final long startCoord = (startCoordOne) | (startCoordOne << coord_size) | (startCoordOne << (coord_size * 2));

            // Format of lightAdditionBlockList word:
            // bbbbb.ggggg.rrrrr.LLLLzzzzzzzyyyyyyyxxxxxxx
            // x/y/z are relative offsets
            if (
                    comp.color.r > saved.color.r | comp.color.g > saved.color.g | comp.color.b > saved.color.b |
                    comp.sun.r > saved.sun.r | comp.sun.g > saved.sun.g | comp.sun.b > saved.sun.b
            ) {
                // compLightValue has components that are larger than savedLightValue, the block at the current position is brighter than the saved value at the current positon... it must have been made brighter somehow
                // Light Splat/Spread

                this.lightAdditionNeeded[offset][offset][offset] = this.updateFlag; // Light needs processing processed
                lightAdditionsCalled++;
                if (DEBUG) {
                    CoreLoadingPlugin.CLLog.warn(
                            "Spread Addition Original " + makePosStr(par_x, par_y, par_z) + " with comp " + makeLightStr(comp)
                                    + " and saved " + makeLightStr(saved));
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
                        int oldOpacity = this.lightAdditionNeededOpacity[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset];
                        if (oldOpacity > 1) {
                            queueLightEntry = this.computeLightValue(queue_x, queue_y, queue_z, par1Enu);
                        }
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
                        
                        @Cleanup Light queue = LightBuffer.getLight();
                        
                        queue.fromLight(queueLightEntry);
                        
                        @Cleanup Light edge = LightBuffer.getLight();
                        
                        edge.fromLight(neighborLightEntry);

                        if (tmp.noL.apply(queue.noL, edge.noL, (a, b) -> a > b ? 1 : 0).reduce(0, (a, b) -> a | b) == 1) {
                            // Components in queueLightEntry are brighter than in edgeLightEntry
                            man_x = MathHelper.abs_int(queue_x - par_x);
                            man_y = MathHelper.abs_int(queue_y - par_y);
                            man_z = MathHelper.abs_int(queue_z - par_z);
                            manhattan_distance = man_x + man_y + man_z;

                            this.setLightValue(par1Enu, queue_x, queue_y, queue_z, queueLightEntry);
                            if (DEBUG) {
                                CoreLoadingPlugin.CLLog.warn("Spread " + makePosStr(queue_x, queue_y, queue_z) + " with queue "
                                        + makeLightStr(queue) + " and edge "
                                        + makeLightStr(edge));
                            }
                            int limit_test = Math.max(Math.max(comp.color.r, comp.color.g), comp.color.b);
                            limit_test = Math.max(Math.max(Math.max(limit_test, comp.sun.r), comp.sun.g), comp.sun.b);

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
                                    int myOpacity = Math.max(1, this.getBlock(queue_x, queue_y, queue_z).getLightOpacity((World) (Object) this, queue_x, queue_y, queue_z));
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

                                            @Cleanup Light queueFiltered = LightBuffer.getLight();

                                            queueFiltered.fromLight(queueLightEntryFiltered);

                                            @Cleanup Light neighbor = LightBuffer.getLight();

                                            neighbor.fromLight(neighborLightEntry);

                                            @Cleanup Light finalLight = LightBuffer.getLight();

                                            finalLight.noL.apply(queue.noL, neighbor.noL, queueFiltered.noL, (q, n, f) -> q > n ? Math.max(0, f) : n);

                                            finalLight.l = 0;

                                            long light_combine = finalLight.toLight();


                                            if ((tmp.noL.apply(finalLight.noL, neighbor.noL, (a, b) -> a > b ? 1 : 0).reduce(0, (a, b) -> a | b) == 1) && (getter < this.lightAdditionBlockList.length)) {
                                                // Mark neighbor to be processed
                                                this.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = this.updateFlag;
                                                this.lightAdditionNeededOpacity[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = myOpacity;
                                                this.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size)
                                                        | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | (light_combine << (coord_size * 3));
                                                lightAdditionsCalled++;
                                            } else {
                                                int finalOpacity = opacity;
                                                if (tmp.noL.apply(queue.noL, neighbor.noL, (a, b) -> a + finalOpacity < b ? 1 : 0).reduce(0, (a, b) -> a | b) == 1) {
                                                    if (Math.abs(queue_x - rel_x) < offset && Math.abs(queue_y - rel_y) < offset && Math.abs(queue_z - rel_z) < offset) {
                                                        // Mark queue location to be re-processed
                                                        this.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = this.updateFlag;
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
            }

            if ((filler > 24389 * 2) || (lightAdditionsCalled != lightAdditionsSatisfied)) {
                CoreLoadingPlugin.CLLog.warn("Error in Light Addition:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue) + " Comp:"
                        + Integer.toBinaryString((int) compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
                        + lightAdditionsSatisfied);
            }

            if (shouldIncrement) { // Only proceed if we are NOT in a recursive call
                this.theProfiler.endStartSection("lightSubtraction");

                // Reset indexes
                filler = 0;
                getter = 0;

                if (tmp.noL.apply(saved.noL, comp.noL, (a, b) -> a > b ? 1 : 0).reduce(0, (a, b) -> a | b) == 1) {
                    // savedLightValue has components that are larger than compLightValue

                    // Light Destruction

                    this.setLightValue(par1Enu, par_x, par_y, par_z, (int) compLightValue); // This kills the light
                    if (DEBUG) {
                        CoreLoadingPlugin.CLLog.warn("Destruction1 " + makePosStr(par_x, par_y, par_z) + " with saved " + makeLightStr(saved)
                                + " and comp " + makeLightStr(comp));
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

                        int limit_test = Math.max(Math.max(saved.color.r, saved.color.g), saved.color.b);
                        limit_test = Math.max(Math.max(Math.max(limit_test, saved.sun.r), saved.sun.g), saved.sun.b);

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

                                @Cleanup Light queue = LightBuffer.getLight();

                                @Cleanup Light neighbor = LightBuffer.getLight();

                                queue.fromLight(queueLightEntry);

                                neighbor.fromLight(neighborLightEntry);

                                if (opacity < 15 || neighborLightEntry > 0) {
                                    // Get Saved light value from face

                                    // |------------------maximum theoretical light value------------------|
                                    // |------saved light value------|
                                    @Cleanup Light finalBlock = LightBuffer.getLight();
                                    finalBlock.l = 0;
                                    int man = man_x + man_y + man_z;
                                    finalBlock.noL.apply(queue.noL, neighbor.noL, (q, n) -> Math.max(q - man, 0) >= n ? 0 : n);

                                    int sortValue = tmp.noL.apply(queue.noL, finalBlock.noL, (q, f) -> ((q > 0) ? f : -1)).reduce(0, Math::max);

                                    long light_combine = finalBlock.toLight();
                                    // If the light we are looking at on the edge is brighter or equal to the
                                    // current light in any way, then there must be a light over there that's doing
                                    // it, so we'll stop eating colors and lights in that direction
                                    if (neighborLightEntry != light_combine) {

                                        if (sortValue != 0) {
                                            finalBlock.noL.applySelf((a) -> a == sortValue ? 0 : a);

                                            light_combine = finalBlock.toLight();

                                            this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag;
                                            this.lightBackfillBlockList[sortValue - 1][this.lightBackfillIndexes[sortValue - 1]++] = (neighbor_x - par_x + size)
                                                    | ((neighbor_y - par_y + size) << coord_size) | ((neighbor_z - par_z + size) << (coord_size * 2)); // record
                                                                                                                                                             // coordinates
                                                                                                                                                             // for
                                                                                                                                                             // backfill
                                        }
                                        this.setLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z, (int) light_combine); // This kills the light
                                        if (DEBUG) {
                                            CoreLoadingPlugin.CLLog.warn("Destruction2 at (X: " + neighbor_x + ", Y: " + neighbor_y + ", Z: " + neighbor_z + ") with (block_r: " + finalBlock.color.r + ", block_g: "
                                                    + finalBlock.color.g + ", block_b: " + finalBlock.color.b + ", sun_r: " + finalBlock.sun.r + ", sun_g: " + finalBlock.sun.g + ", sun_b: " + finalBlock.sun.b + ")");
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
                        CoreLoadingPlugin.CLLog.warn("Light Subtraction Overfilled:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue)
                                + " Comp:" + Integer.toBinaryString((int) compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
                                + lightAdditionsSatisfied);
                    }

                    this.theProfiler.endStartSection("lightBackfill");

                    // Backfill
                    for (filler = this.lightBackfillIndexes.length - 1; filler >= 0; filler--) {
                        while (this.lightBackfillIndexes[filler] > 0) {
                            getter = this.lightBackfillBlockList[filler][--this.lightBackfillIndexes[filler]];
                            queue_x = (getter & coord_mask) - size + par_x; // Get Entry X coord
                            queue_y = (getter >> coord_size & coord_mask) - size + par_y; // Get Entry Y coord
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
        if (CoreDummyContainer.getDynamicLight != null && world.isRemote) {
            try {
                int a = (Integer) CoreDummyContainer.getDynamicLight.invoke(null, world, block, par_x, par_y, par_z);
                if (a != 0)
                    CoreLoadingPlugin.CLLog.info("got :" + a);
                return a;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return block.getLightValue(world, par_x, par_y, par_z);
    }
}
