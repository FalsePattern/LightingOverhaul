package lightingoverhaul.mixin.mixins.common.minecraft;

import com.google.common.primitives.Ints;
import lightingoverhaul.Config;
import lightingoverhaul.api.LightingApi;
import lightingoverhaul.LightingOverhaul;
import lightingoverhaul.mixin.interfaces.IChunkMixin;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;

import static lightingoverhaul.LightingOverhaul.LOlog;

@SuppressWarnings("ConstantConditions")
@Mixin(World.class)
public abstract class WorldMixin {
    @Final
    @Shadow
    public Profiler theProfiler;

    @Shadow
    public abstract Chunk getChunkFromChunkCoords(int paramInt1, int paramInt2);

    @Shadow
    public abstract boolean canBlockSeeTheSky(int x, int y, int z);

    @Shadow
    public abstract Block getBlock(int x, int y, int z);

    private Block getBlockNullSafe(int x, int y, int z) {
        Block block = getBlock(x, y, z);
        return block == null ? Blocks.air : block;
    }

    @Shadow
    public abstract int getSavedLightValue(EnumSkyBlock paramEnumSkyBlock, int x, int y, int z);

    @Shadow
    public abstract void setLightValue(EnumSkyBlock paramEnumSkyBlock, int x, int y, int z, int value);

    @Shadow
    public abstract int getBlockMetadata(int x, int y, int z);

    @Shadow
    public abstract boolean doChunksNearChunkExist(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    @Shadow
    protected abstract int computeLightValue(int p_98179_1_, int p_98179_2_, int p_98179_3_, EnumSkyBlock p_98179_4_);

    public long[] lightAdditionBlockList;
    public int[][][] lightAdditionNeeded;
    public int[][][] lightAdditionNeededOpacity;
    public int[] lightBackfillIndexes;
    public int[][] lightBackfillBlockList;
    public int[][][] lightBackfillNeeded;
    public int updateFlag;
    public EnumSkyBlock flagEntry;
    private boolean inited;

    // white orange magenta lightblue yellow lime pink gray lightgray cyan purple
    // blue brown green red black
    int[] stainedglass_api_index;

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Inject(at = @At("RETURN"), method = { "<init>*" })
    public void init(CallbackInfo callbackInfo) {
        doInit();
    }

    private void doInit() {
        if (!inited) {
            lightAdditionBlockList = new long[32768 * 8];
            lightAdditionNeeded = new int[64][64][64];
            lightAdditionNeededOpacity = new int[64][64][64];
            lightBackfillIndexes = new int[32];
            lightBackfillBlockList = new int[32][5000 * 8];
            lightBackfillNeeded = new int[64][64][64];
            updateFlag = 1;
            flagEntry = EnumSkyBlock.Block;
            stainedglass_api_index = new int[] { 15, 14, 13, 12, 11, 10, 9, 7, 8, 6, 5, 4, 3, 2, 1, 0 };
            inited = true;
        }
    }



    private int calculateOpacity(int lightIn, int opacityIn, Block block, int x, int y, int z) {
        int r = LightingApi.extractR(lightIn);
        int g = LightingApi.extractG(lightIn);
        int b = LightingApi.extractB(lightIn);
        int sr = LightingApi.extractSunR(lightIn);
        int sg = LightingApi.extractSunG(lightIn);
        int sb = LightingApi.extractSunB(lightIn);

        boolean hasColor = r > 0 || g > 0 || b > 0;

        int opacity_r = opacityIn;
        int opacity_g = opacityIn;
        int opacity_b = opacityIn;

        if (block instanceof BlockStainedGlass) {
            int meta = this.getBlockMetadata(x, y, z);
            int index = stainedglass_api_index[meta];
            opacity_r = Math.round((15 - LightingApi.r[index]) / 3.0F) + 1;
            opacity_g = Math.round((15 - LightingApi.g[index]) / 3.0F) + 1;
            opacity_b = Math.round((15 - LightingApi.b[index]) / 3.0F) + 1;
        }
        r = Math.max(0, r - opacity_r);
        g = Math.max(0, g - opacity_g);
        b = Math.max(0, b - opacity_b);
        sr = Math.max(0, sr - opacity_r);
        sg = Math.max(0, sg - opacity_g);
        sb = Math.max(0, sb - opacity_b);

        return LightingApi.toLight(r, g, b, sr, sg, sb);
    }
    @Inject(method = "computeLightValue",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void computeLightValueReplacement(int x, int y, int z, EnumSkyBlock par1Enu, CallbackInfoReturnable<Integer> cir) {
        boolean isSky = par1Enu == EnumSkyBlock.Sky;
        Chunk chunk = getChunkFromChunkCoords(x >> 4, z >> 4);
        IChunkMixin chunkMixin = (IChunkMixin) chunk;
        if (isSky && this.canBlockSeeTheSky(x, y, z)) {
            cir.setReturnValue(EnumSkyBlock.Sky.defaultLightValue);
        } else {
            Block block = this.getBlockNullSafe(x, y, z);

            int currentLight = 0;
            if (!isSky) {
                currentLight = (block == null ? 0 : this.getLightValueSomehow(block, (World) (Object) this, x, y, z));
                if ((currentLight > 0) && (currentLight <= 0xF)) {
                    // copy vanilla brightness into each color component to make it white/grey if it is uncolored.
                    currentLight = LightingApi.toLightBlock(currentLight, currentLight, currentLight);
                }
            }

            int current_r;
            int current_g;
            int current_b;

            if (isSky) {
                current_r = LightingApi.extractSunR(currentLight);
                current_g = LightingApi.extractSunG(currentLight);
                current_b = LightingApi.extractSunB(currentLight);
                if (chunkMixin.canReallySeeTheSky(x & 0xF, y, z & 0xF)) {
                    int sun_precomputed = chunkMixin.getRealSunColor(x & 0xF, y, z & 0xF);

                    int sun2_r = LightingApi.extractSunR(sun_precomputed);
                    int sun2_g = LightingApi.extractSunG(sun_precomputed);
                    int sun2_b = LightingApi.extractSunB(sun_precomputed);

                    current_r = Math.max(current_r, sun2_r);
                    current_g = Math.max(current_g, sun2_g);
                    current_b = Math.max(current_b, sun2_b);
                }
            } else {
                current_r = LightingApi.extractR(currentLight);
                current_g = LightingApi.extractG(currentLight);
                current_b = LightingApi.extractB(currentLight);
            }

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
                        if (getBlockNullSafe(l1, i2, j2) instanceof BlockStainedGlass) {
                            if (isSky && chunkMixin.canReallySeeTheSky(l1 & 0xF, i2, j2 & 0xF))
                                continue;
                        }
                    }

                    int neighborLight = this.getSavedLightValue(par1Enu, l1, i2, j2);

                    int light = calculateOpacity(neighborLight, opacity, block, x, y, z);

                    int neighbor_r;
                    int neighbor_g;
                    int neighbor_b;
                    if (isSky) {
                        neighbor_r = LightingApi.extractSunR(light);
                        neighbor_g = LightingApi.extractSunG(light);
                        neighbor_b = LightingApi.extractSunB(light);
                    } else {
                        neighbor_r = LightingApi.extractR(light);
                        neighbor_g = LightingApi.extractG(light);
                        neighbor_b = LightingApi.extractB(light);
                    }
                    current_r = Math.max(current_r, neighbor_r);
                    current_g = Math.max(current_g, neighbor_g);
                    current_b = Math.max(current_b, neighbor_b);
                }

                cir.setReturnValue(isSky ? LightingApi.toLightSun(current_r, current_g, current_b) : LightingApi.toLightBlock(current_r, current_g, current_b));
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

    public boolean updateLightByType_withIncrement(EnumSkyBlock par1Enu, int par_x, int par_y, int par_z, boolean shouldIncrement, int rel_x, int rel_y, int rel_z) {
        doInit();
        if (!this.doChunksNearChunkExist(par_x, par_y, par_z, 17)) {
            return false;
        }
        if (par1Enu == null) par1Enu = EnumSkyBlock.Block;
        boolean isSky = par1Enu == EnumSkyBlock.Sky;
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

        int savedLightValue = this.getSavedLightValue(par1Enu, par_x, par_y, par_z);
        int compLightValue = this.computeLightValue(par_x, par_y, par_z, par1Enu);
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

        this.theProfiler.endStartSection("lightAddition");

        int saved_r;
        int saved_g;
        int saved_b;
        int comp_r;
        int comp_g;
        int comp_b;
        if (isSky) {
            saved_r = LightingApi.extractSunR(savedLightValue);
            saved_g = LightingApi.extractSunG(savedLightValue);
            saved_b = LightingApi.extractSunB(savedLightValue);
            comp_r = LightingApi.extractSunR(compLightValue);
            comp_g = LightingApi.extractSunG(compLightValue);
            comp_b = LightingApi.extractSunB(compLightValue);
        } else {
            saved_r = LightingApi.extractR(savedLightValue);
            saved_g = LightingApi.extractG(savedLightValue);
            saved_b = LightingApi.extractB(savedLightValue);
            comp_r = LightingApi.extractR(compLightValue);
            comp_g = LightingApi.extractG(compLightValue);
            comp_b = LightingApi.extractB(compLightValue);
        }


        final int offset = 15; // Offset for the start block
        final int size = 32; // Number or blocks in one directon
        final int coord_size = 6; // Number of bits for one axis
        final int coord_mask = (1 << coord_size) - 1; // Mask for getting one axis
        final int startCoordOne = (1 << coord_size) / 2;
        final long startCoord = (startCoordOne) | (startCoordOne << coord_size) | (startCoordOne << (coord_size * 2));

        // Format of lightAdditionBlockList word:
        // ......................bbbbggggrrrrBBBBGGGGRRRRzzzzzzyyyyyyxxxxxx
        // x/y/z are relative offsets
        if (comp_r > saved_r | comp_g > saved_g | comp_b > saved_b) {
            // compLightValue has components that are larger than savedLightValue, the block at the current position is brighter than the saved value at the current positon... it must have been made brighter somehow
            // Light Splat/Spread

            this.lightAdditionNeeded[offset][offset][offset] = this.updateFlag; // Light needs processing processed
            lightAdditionsCalled++;

            this.lightAdditionBlockList[getter++] = startCoord | ((long) compLightValue << (coord_size * 3));

            while (filler < getter) {
                queueEntry = this.lightAdditionBlockList[filler++]; // Get Entry at l, which starts at 0
                queue_x = ((int) (queueEntry & coord_mask) - size + par_x); // Get Entry X coord
                queue_y = ((int) ((queueEntry >> coord_size) & coord_mask) - size + par_y); // Get Entry Y coord
                queue_z = ((int) ((queueEntry >> (coord_size * 2)) & coord_mask) - size + par_z); // Get Entry Z
                // coord

                if (this.lightAdditionNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] != this.updateFlag)
                    continue; // Light has been marked for a later update

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

                int queue_r;
                int queue_g;
                int queue_b;
                int edge_r;
                int edge_g;
                int edge_b;
                if (isSky) {
                    queue_r = LightingApi.extractSunR(queueLightEntry);
                    queue_g = LightingApi.extractSunG(queueLightEntry);
                    queue_b = LightingApi.extractSunB(queueLightEntry);
                    edge_r = LightingApi.extractSunR(neighborLightEntry);
                    edge_g = LightingApi.extractSunG(neighborLightEntry);
                    edge_b = LightingApi.extractSunB(neighborLightEntry);
                } else {
                    queue_r = LightingApi.extractR(queueLightEntry);
                    queue_g = LightingApi.extractG(queueLightEntry);
                    queue_b = LightingApi.extractB(queueLightEntry);
                    edge_r = LightingApi.extractR(neighborLightEntry);
                    edge_g = LightingApi.extractG(neighborLightEntry);
                    edge_b = LightingApi.extractB(neighborLightEntry);
                }

                if (queue_r > edge_r || queue_g > edge_g || queue_b > edge_b) {
                    // Components in queueLightEntry are brighter than in edgeLightEntry
                    man_x = MathHelper.abs_int(queue_x - par_x);
                    man_y = MathHelper.abs_int(queue_y - par_y);
                    man_z = MathHelper.abs_int(queue_z - par_z);
                    manhattan_distance = man_x + man_y + man_z;

                    this.setLightValue(par1Enu, queue_x, queue_y, queue_z, queueLightEntry);

                    int limit_test = Math.max(Math.max(comp_r, comp_g), comp_b);
                    limit_test = Math.max(Math.max(Math.max(limit_test, comp_r), comp_g), comp_b);

                    // if ((manhattan_distance < ((compLightValue & 0x0000F) - 1)) || (par1Enu ==
                    // EnumSkyBlock.Sky && (man_x<14) && (man_y<14) && (man_z<14))) { //Limits the
                    // splat size to the initial brightness value, skylight checks bypass this, as
                    // they aren't always diamond-shaped
                    if (manhattan_distance >= limit_test - 1)
                        continue;
                    for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                        neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                        neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                        neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                        if (neighbor_y < 0 || neighbor_y > 255)
                            continue;

                        lightEntry = this.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset];
                        int myOpacity = Math.max(1, this.getBlockNullSafe(queue_x, queue_y, queue_z).getLightOpacity((World) (Object) this, queue_x, queue_y, queue_z));

                        // on recursive calls, ignore instances of this.updateFlag being flag + 1
                        if (lightEntry == this.updateFlag || (lightEntry == this.updateFlag + 1 && shouldIncrement))
                            continue;
                        Block neighborBlock = this.getBlockNullSafe(neighbor_x, neighbor_y, neighbor_z);
                        opacity = Math.max(1, neighborBlock.getLightOpacity((World) (Object) this, neighbor_x, neighbor_y, neighbor_z));

                        // Proceed only if the block is non-solid
                        if (opacity >= 15)
                            continue;

                        // Get Saved light value from face
                        neighborLightEntry = this.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);

                        int queueLightEntryFiltered = calculateOpacity(queueLightEntry, opacity, neighborBlock, neighbor_x, neighbor_y, neighbor_z);

                        int queueFiltered_r;
                        int queueFiltered_g;
                        int queueFiltered_b;
                        int neighbor_r;
                        int neighbor_g;
                        int neighbor_b;
                        if (isSky) {
                            queueFiltered_r = LightingApi.extractSunR(queueLightEntryFiltered);
                            queueFiltered_g = LightingApi.extractSunG(queueLightEntryFiltered);
                            queueFiltered_b = LightingApi.extractSunB(queueLightEntryFiltered);
                            neighbor_r = LightingApi.extractSunR(neighborLightEntry);
                            neighbor_g = LightingApi.extractSunG(neighborLightEntry);
                            neighbor_b = LightingApi.extractSunB(neighborLightEntry);
                        } else {
                            queueFiltered_r = LightingApi.extractR(queueLightEntryFiltered);
                            queueFiltered_g = LightingApi.extractG(queueLightEntryFiltered);
                            queueFiltered_b = LightingApi.extractB(queueLightEntryFiltered);
                            neighbor_r = LightingApi.extractR(neighborLightEntry);
                            neighbor_g = LightingApi.extractG(neighborLightEntry);
                            neighbor_b = LightingApi.extractB(neighborLightEntry);
                        }


                        int final_r = queue_r > neighbor_r ? Math.max(0, queueFiltered_r) : neighbor_r;
                        int final_g = queue_g > neighbor_g ? Math.max(0, queueFiltered_g) : neighbor_g;
                        int final_b = queue_b > neighbor_b ? Math.max(0, queueFiltered_b) : neighbor_b;

                        long light_combine = isSky ? LightingApi.toLightSun(final_r, final_g, final_b) : LightingApi.toLightBlock(final_r, final_g, final_b);

                        if (((final_r > neighbor_r) || (final_g > neighbor_g) || (final_b > neighbor_b)) && (getter < this.lightAdditionBlockList.length)) {
                            // Mark neighbor to be processed
                            this.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = this.updateFlag;
                            this.lightAdditionNeededOpacity[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = myOpacity;
                            this.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size)
                                                                    | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | (light_combine << (coord_size * 3));
                            lightAdditionsCalled++;
                        } else {
                            if (queue_r + opacity >= neighbor_r && queue_g + opacity >= neighbor_g && queue_b + opacity >= neighbor_b)
                                continue;
                            if (Math.abs(queue_x - rel_x) >= offset || Math.abs(queue_y - rel_y) >= offset || Math.abs(queue_z - rel_z) >= offset)
                                continue;
                            // Mark queue location to be re-processed
                            this.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = this.updateFlag;
                        }
                    }
                }
            }
        }

        if (Config.debug() && ((filler > 24389 * 2) || (lightAdditionsCalled != lightAdditionsSatisfied))) {
            LOlog.warn("Error in Light Addition:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString(savedLightValue) + " Comp:"
                       + Integer.toBinaryString(compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
                       + lightAdditionsSatisfied);
        }

        if (shouldIncrement) { // Only proceed if we are NOT in a recursive call
            this.theProfiler.endStartSection("lightSubtraction");

            // Reset indexes
            filler = 0;
            getter = 0;

            if (saved_r > comp_r || saved_g > comp_g || saved_b > comp_b) {
                // savedLightValue has components that are larger than compLightValue

                // Light Destruction

                this.setLightValue(par1Enu, par_x, par_y, par_z, compLightValue); // This kills the light

                this.lightAdditionBlockList[getter++] = (startCoord | ((long) savedLightValue << (coord_size * 3)));

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

                    int limit_test = Math.max(Math.max(saved_r, saved_g), saved_b);

                    if (manhattan_distance >= limit_test) // Limits the splat size to the initial brightness value
                        continue;
                    for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                        neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                        neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                        neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                        if (neighbor_y < 0 || neighbor_y > 255)
                            continue;

                        man_x = MathHelper.abs_int(neighbor_x - par_x);
                        man_y = MathHelper.abs_int(neighbor_y - par_y);
                        man_z = MathHelper.abs_int(neighbor_z - par_z);

                        opacity = Math.max(1, this.getBlockNullSafe(neighbor_x, neighbor_y, neighbor_z).getLightOpacity((World) (Object) this, neighbor_x, neighbor_y, neighbor_z));
                        neighborLightEntry = this.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);
                        neighborLightEntry = neighborLightEntry & ~0xF;
                        int queue_r;
                        int queue_g;
                        int queue_b;
                        int neighbor_r;
                        int neighbor_g;
                        int neighbor_b;
                        if (isSky) {
                            queue_r = LightingApi.extractSunR(queueLightEntry);
                            queue_g = LightingApi.extractSunG(queueLightEntry);
                            queue_b = LightingApi.extractSunB(queueLightEntry);
                            neighbor_r = LightingApi.extractSunR(neighborLightEntry);
                            neighbor_g = LightingApi.extractSunG(neighborLightEntry);
                            neighbor_b = LightingApi.extractSunB(neighborLightEntry);
                        } else {
                            queue_r = LightingApi.extractR(queueLightEntry);
                            queue_g = LightingApi.extractG(queueLightEntry);
                            queue_b = LightingApi.extractB(queueLightEntry);
                            neighbor_r = LightingApi.extractR(neighborLightEntry);
                            neighbor_g = LightingApi.extractG(neighborLightEntry);
                            neighbor_b = LightingApi.extractB(neighborLightEntry);
                        }

                        if (opacity >= 15 && neighborLightEntry <= 0)
                            continue;
                        // Get Saved light value from face

                        // |------------------maximum theoretical light value------------------|
                        // |------saved light value------|
                        int man = man_x + man_y + man_z;
                        int final_r = Math.max(queue_r - man, 0) >= neighbor_r ? 0 : neighbor_r;
                        int final_g = Math.max(queue_g - man, 0) >= neighbor_g ? 0 : neighbor_g;
                        int final_b = Math.max(queue_b - man, 0) >= neighbor_b ? 0 : neighbor_b;

                        int sortValue = Ints.max(queue_r > 0 ? final_r : 0, queue_g > 0 ? final_g : 0, queue_b > 0 ? final_b : 0);
                        int light_combine = isSky ? LightingApi.toLightSun(final_r, final_g, final_b) : LightingApi.toLightBlock(final_r, final_g, final_b);
                        // If the light we are looking at on the edge is brighter or equal to the
                        // current light in any way, then there must be a light over there that's doing
                        // it, so we'll stop eating colors and lights in that direction
                        if (neighborLightEntry != light_combine) {

                            if (sortValue != 0) {
                                final_r = final_r == sortValue ? 0 : final_r;
                                final_g = final_g == sortValue ? 0 : final_g;
                                final_b = final_b == sortValue ? 0 : final_b;

                                light_combine = isSky ? LightingApi.toLightSun(final_r, final_g, final_b) : LightingApi.toLightBlock(final_r, final_g, final_b);

                                // record coordinates for backfill
                                this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag;
                                this.lightBackfillBlockList[sortValue - 1][this.lightBackfillIndexes[sortValue - 1]++] = (neighbor_x - par_x + size)
                                                                                                                         | ((neighbor_y - par_y + size) << coord_size) | ((neighbor_z - par_z + size) << (coord_size * 2));
                            }
                            this.setLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z, light_combine); // This kills the light

                            // this array keeps the algorithm going, don't touch
                            this.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size)
                                                                    | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | ((long) queueLightEntry << (coord_size * 3));

                        } else {
                            if (sortValue == 0)
                                continue;

                            // record coordinates for backfill
                            this.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = this.updateFlag;
                            this.lightBackfillBlockList[sortValue - 1][this.lightBackfillIndexes[sortValue - 1]++] = (queue_x - par_x + size) | ((queue_y - par_y + size) << coord_size)
                                                                                                                     | ((queue_z - par_z + size) << (coord_size * 2));
                        }
                    }
                }

                if (Config.debug() && filler > 4097 * 2) {
                    LOlog.warn("Light Subtraction Overfilled:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString(savedLightValue)
                               + " Comp:" + Integer.toBinaryString(compLightValue) + " isBackfill:" + " updateFlag:" + this.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:"
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

    /**
     * Patching in Dynamic Lights Compatibility
     */
    private int getLightValueSomehow(Block block, World world, int par_x, int par_y, int par_z) {
        if (LightingOverhaul.getDynamicLight != null && world.isRemote) {
            try {
                return (int) LightingOverhaul.getDynamicLight.invoke(null, world, block, par_x, par_y, par_z);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return block.getLightValue(world, par_x, par_y, par_z);
    }
}
