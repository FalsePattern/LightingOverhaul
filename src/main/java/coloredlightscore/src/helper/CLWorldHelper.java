package coloredlightscore.src.helper;

import coloredlightscore.src.api.CLApi;
import coloredlightscore.src.asm.ColoredLightsCoreDummyContainer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;

import static coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin.CLLog;

public class CLWorldHelper {
    //Copied from the world class in 1.7.2, modified from the source from 1.6.4, made the method STATIC
    //Added the parameter 'World world, ' and then replaces all instances of world, with WORLD
    public static int getBlockLightValue_do(World world, int x, int y, int z, boolean par4) {
        if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
            if (par4 && world.getBlock(x, y, z).getUseNeighborBrightness()) {
                // heaton84 - should be world.getBlockLightValue_do,
                // switched to CLWorldHelper.getBlockLightValue_do
                // This will save an extra invoke
                int l1 = CLWorldHelper.getBlockLightValue_do(world, x, y + 1, z, false);
                int l = CLWorldHelper.getBlockLightValue_do(world, x + 1, y, z, false);
                int i1 = CLWorldHelper.getBlockLightValue_do(world, x - 1, y, z, false);
                int j1 = CLWorldHelper.getBlockLightValue_do(world, x, y, z + 1, false);
                int k1 = CLWorldHelper.getBlockLightValue_do(world, x, y, z - 1, false);

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

                //int cx = x >> 4;
                //int cz = z >> 4;
                Chunk chunk = world.pipe.getChunkFromChunkCoords(x >> 4, z >> 4);
                x &= 0xf;
                z &= 0xf;

                //CLLog.info("NEWTEST {},{}:{}", cx, cz, Integer.toBinaryString(chunk.getBlockLightValue(0, 0, 0, 15)));

                return chunk.getBlockLightValue(x, y, z, world.skylightSubtracted);
            }
        } else {
            return 15;
        }
    }
    //                                               white  orange  magenta lightblue yellow lime pink gray lightgray cyan purple blue brown green red black
    static int[] stainedglass_api_index = new int[] {   15,     14,      13,      12,     11,  10,   9,   7,        8,   6,     5,   4,    3,    2,  1,    0 };

    private static int calculateOpacity(int light, int opacity, Block block, World world, int x, int y, int z)
    {
        int l = (light >> CLApi.bitshift_l) & 0xF;
        int r = (light >> CLApi.bitshift_r) & CLApi.bitmask;
        int g = (light >> CLApi.bitshift_g) & CLApi.bitmask;
        int b = (light >> CLApi.bitshift_b) & CLApi.bitmask;

        boolean hasColor = r > 0 || g > 0 || b > 0;

        int r_opacity = opacity;
        int g_opacity = opacity;
        int b_opacity = opacity;

        if (block instanceof BlockStainedGlass)
        {
            int meta = world.getBlockMetadata(x, y, z);
            int index = stainedglass_api_index[meta];
            r_opacity = (int)Math.round((15 - CLApi.r[index]) / 3.0f) + 1;
            g_opacity = (int)Math.round((15 - CLApi.g[index]) / 3.0f) + 1;
            b_opacity = (int)Math.round((15 - CLApi.b[index]) / 3.0f) + 1;
        }

        l =  Math.max(0, l - opacity);
        r =  Math.max(0, r - r_opacity);
        g =  Math.max(0, g - g_opacity);
        b =  Math.max(0, b - b_opacity);

        if (hasColor && r + g + b == 0)
            l = 0;

        if (r > 15 || g > 15 || b > 15)
            l = 15;

        return (l << CLApi.bitshift_l) | (r << CLApi.bitshift_r) | (g << CLApi.bitshift_g) | (b << CLApi.bitshift_b);
    }

    //Use this one if you want color
    @SideOnly(Side.CLIENT)
    public static int getLightBrightnessForSkyBlocks(World world, int x, int y, int z, int lightValue) {
        int skyBrightness = world.pipe.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = world.pipe.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        int light_l = (lightValue >> CLApi.bitshift_l) & 0xF;
        int light_r = (lightValue >> CLApi.bitshift_r) & CLApi.bitmask;
        int light_g = (lightValue >> CLApi.bitshift_g) & CLApi.bitmask;
        int light_b = (lightValue >> CLApi.bitshift_b) & CLApi.bitmask;

        int block_l = (blockBrightness >> CLApi.bitshift_l) & 0xF;
        int block_r = (blockBrightness >> CLApi.bitshift_r) & CLApi.bitmask;
        int block_g = (blockBrightness >> CLApi.bitshift_g) & CLApi.bitmask;
        int block_b = (blockBrightness >> CLApi.bitshift_b) & CLApi.bitmask;

        block_l = Math.max(block_l,  light_l);
        block_r = Math.max(block_r,  light_r);
        block_g = Math.max(block_g,  light_g);
        block_b = Math.max(block_b,  light_b);

        return (skyBrightness << CLApi.bitshift_s2)
                | (block_l << CLApi.bitshift_l2) | (block_r << CLApi.bitshift_r2) | (block_g << CLApi.bitshift_g2) | (block_b << CLApi.bitshift_b2);
    }

    public static int computeLightValue(World world, int par_x, int par_y, int par_z, EnumSkyBlock par1Enu) {
        if (par1Enu == EnumSkyBlock.Sky && world.pipe.canBlockSeeTheSky(par_x, par_y, par_z)) {
            return 15;
        } else {
            Block block = world.getBlock(par_x, par_y, par_z);

            int currentLight = 0;
            if (par1Enu != EnumSkyBlock.Sky) {
                currentLight = (block == null ? 0 : getLightValueSomehow(block, world, par_x, par_y, par_z));
                if ((currentLight > 0) && (currentLight <= 0xF)) {
                    currentLight = (currentLight << CLApi.bitshift_r) | (currentLight << CLApi.bitshift_g) | (currentLight << CLApi.bitshift_b) | (currentLight << CLApi.bitshift_l); //copy vanilla brightness into each color component to make it white/grey if it is uncolored.
                }
            }

            int l = (currentLight >> CLApi.bitshift_l) & 0xF;
            int r = (currentLight >> CLApi.bitshift_r) & CLApi.bitmask;
            int g = (currentLight >> CLApi.bitshift_g) & CLApi.bitmask;
            int b = (currentLight >> CLApi.bitshift_b) & CLApi.bitmask;

            if (r > 15 || g > 15 || b > 15)
                l = 15;

            currentLight |= (l << CLApi.bitshift_l);

            int opacity = (block == null ? 0 : block.getLightOpacity(world, par_x, par_y, par_z));

            if (opacity >= 15 && currentLight > 0) {
                opacity = 1;
            }

            if (opacity < 1) {
                opacity = 1;
            }

            if (opacity >= 15) {
                return 0;
            }
            else if ((currentLight & 15) >= 14) {
                return currentLight;
            }
            else {

                for (int faceIndex = 0; faceIndex < 6; ++faceIndex) {
                    int l1 = par_x + Facing.offsetsXForSide[faceIndex];
                    int i2 = par_y + Facing.offsetsYForSide[faceIndex];
                    int j2 = par_z + Facing.offsetsZForSide[faceIndex];

                    int neighborLight = world.pipe.getSavedLightValue(par1Enu, l1, i2, j2);

                    int light = calculateOpacity(neighborLight, opacity, block, world, par_x, par_y, par_z);

                    int l2 = (light >> CLApi.bitshift_l) & 0xF;
                    int r2 = (light >> CLApi.bitshift_r) & CLApi.bitmask;
                    int g2 = (light >> CLApi.bitshift_g) & CLApi.bitmask;
                    int b2 = (light >> CLApi.bitshift_b) & CLApi.bitmask;

                    l = Math.max(l, l2);
                    r = Math.max(r, r2);
                    g = Math.max(g, g2);
                    b = Math.max(b, b2);
                }
                return (l << CLApi.bitshift_l) | (r << CLApi.bitshift_r) | (g << CLApi.bitshift_g) | (b << CLApi.bitshift_b);
            }
        }
    }

    public static boolean updateLightByType(World world, EnumSkyBlock par1Enu, int par_x, int par_y, int par_z) {
        return CLWorldHelper.updateLightByType_withIncrement(world, par1Enu, par_x, par_y, par_z, true, par_x, par_y, par_z);
    }

    public static boolean updateLightByType_withIncrement(World world, EnumSkyBlock par1Enu, int par_x, int par_y, int par_z, boolean shouldIncrement, int rel_x, int rel_y, int rel_z) {
        if (!world.pipe.doChunksNearChunkExist(par_x, par_y, par_z, 17)) {
            return false;
        } else {

            if (shouldIncrement) {
                //Increment the updateFlag ONLY on a fresh call... This keeps the updateFlag consistent when the algorithm recurses
                // if ((flag_entry != updateFlag) && (flag_entry != updateFlag+1)) { // Light has not been visited by the algorithm yet
                // if (flag_entry == updateFlag) { // Light has been marked for a later update
                // if (flag_entry == updateFlag+1) { // Light has been visited and processed, don't visit in the future generations of this algorithm
                world.pipe.updateFlag += 2;
                world.pipe.flagEntry = par1Enu;
            }

            world.theProfiler.startSection("getBrightness");

            int lightAdditionsSatisfied = 0;
            int lightAdditionsCalled = 0;
            int filler = 0;
            int getter = 0;
            int lightEntry;

            long savedLightValue = world.pipe.getSavedLightValue(par1Enu, par_x, par_y, par_z);
            long compLightValue = CLWorldHelper.computeLightValue(world, par_x, par_y, par_z, par1Enu);
            int r = (int)(compLightValue >> CLApi.bitshift_r) & CLApi.bitmask;
            long queueEntry;
            int queue_x;
            int queue_y;
            int queue_z;
            int queueLightEntry;

            int man_x;
            int man_y;
            int man_z;
            long manhattan_distance;

            long ll;
            long rl;
            long gl;
            long bl;
            int sortValue;
            int opacity;

            int neighbor_x;
            int neighbor_y;
            int neighbor_z;

            int neighborIndex;
            int neighborLightEntry;

            world.theProfiler.endStartSection("lightAddition");

            int saved_l = (int)(savedLightValue >> CLApi.bitshift_l) & 0xF;
            int saved_r = (int)(savedLightValue >> CLApi.bitshift_r) & CLApi.bitmask;
            int saved_g = (int)(savedLightValue >> CLApi.bitshift_g) & CLApi.bitmask;
            int saved_b = (int)(savedLightValue >> CLApi.bitshift_b) & CLApi.bitmask;

            int comp_l = (int)(compLightValue >> CLApi.bitshift_l) & 0xF;
            int comp_r = (int)(compLightValue >> CLApi.bitshift_r) & CLApi.bitmask;
            int comp_g = (int)(compLightValue >> CLApi.bitshift_g) & CLApi.bitmask;
            int comp_b = (int)(compLightValue >> CLApi.bitshift_b) & CLApi.bitmask;

            final int offset = 31; // Offset for the start block
            final int size = 64; // Number or blocks in one directon
            final int coord_size = 7; // Number of bits for one axis
            final int coord_mask = (1 << coord_size) - 1; // Mask for getting one axis
            final int startCoordOne = (1 << coord_size) / 2;
            final long startCoord = (startCoordOne) | (startCoordOne << coord_size) | (startCoordOne << (coord_size * 2));

            // Format of lightAdditionBlockList word:
            // bbbbb.ggggg.rrrrr.LLLLzzzzzzzyyyyyyyxxxxxxx
            // x/y/z are relative offsets
            if (comp_l > saved_l || comp_r > saved_r || comp_g > saved_g || comp_b > saved_b) { //compLightValue has components that are larger than savedLightValue, the block at the current position is brighter than the saved value at the current positon... it must have been made brighter somehow
                //Light Splat/Spread

                world.pipe.lightAdditionNeeded[offset][offset][offset] = world.pipe.updateFlag; // Light needs processing processed
                lightAdditionsCalled++;
                world.pipe.lightAdditionBlockList[getter++] = startCoord | (compLightValue << (coord_size * 3));

                while (filler < getter) {
                    queueEntry = world.pipe.lightAdditionBlockList[filler++]; //Get Entry at l, which starts at 0
                    queue_x = ((int) (queueEntry & coord_mask) - size + par_x); //Get Entry X coord
                    queue_y = ((int) ((queueEntry >> coord_size) & coord_mask) - size + par_y); //Get Entry Y coord
                    queue_z = ((int) ((queueEntry >> (coord_size * 2)) & coord_mask) - size + par_z); //Get Entry Z coord

                    if (world.pipe.lightAdditionNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] == world.pipe.updateFlag) { // Light has been marked for a later update

                        queueLightEntry = (int) (queueEntry >>> (coord_size * 3)); //Get Entry's saved Light
                        neighborLightEntry = world.pipe.getSavedLightValue(par1Enu, queue_x, queue_y, queue_z); //Get the saved Light Level at the entry's location - Instead of comparing against the value saved on disk every iteration, and checking to see if it's been updated already... Consider storing values in a temp 3D array as they are gathered and applying changes all at once

                        if (Math.abs(queue_x - rel_x) < offset && Math.abs(queue_y - rel_y) < offset && Math.abs(queue_z - rel_z) < offset) {
                            world.pipe.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = world.pipe.updateFlag + 1; // Light has been visited and processed
                        }
                        world.pipe.lightAdditionNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = world.pipe.updateFlag + 1; // Light has been visited and processed

                        lightAdditionsSatisfied++;

                        int queue_l = (queueLightEntry >> CLApi.bitshift_l) & 0xF;
                        int queue_r = (queueLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                        int queue_g = (queueLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                        int queue_b = (queueLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;

                        int edge_l = (int)(neighborLightEntry >> CLApi.bitshift_l) & 0xF;
                        int edge_r = (int)(neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                        int edge_g = (int)(neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                        int edge_b = (int)(neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;

                        if (queue_l > edge_l || queue_r > edge_r || queue_g > edge_g || queue_b > edge_b) { // Components in queueLightEntry are brighter than in edgeLightEntry
                            man_x = MathHelper.abs_int(queue_x - par_x);
                            man_y = MathHelper.abs_int(queue_y - par_y);
                            man_z = MathHelper.abs_int(queue_z - par_z);
                            manhattan_distance = man_x + man_y + man_z;

                            world.pipe.setLightValue(par1Enu, queue_x, queue_y, queue_z, queueLightEntry);

                            int limit_test = Math.max(Math.max(Math.max(comp_l, comp_r), comp_g), comp_b);

                            //if ((manhattan_distance < ((compLightValue & 0x0000F) - 1)) || (par1Enu == EnumSkyBlock.Sky && (man_x<14) && (man_y<14) && (man_z<14))) { //Limits the splat size to the initial brightness value, skylight checks bypass this, as they aren't always diamond-shaped
                            if (manhattan_distance < limit_test - 1) {
                                for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                                    neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                                    neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                                    neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                                    if (neighbor_y < 0 || neighbor_y > 255)
                                        continue;

                                    lightEntry = world.pipe.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset];
                                    if (lightEntry != world.pipe.updateFlag && (lightEntry != world.pipe.updateFlag + 1 || !shouldIncrement)) { // on recursive calls, ignore instances of world.pipe.updateFlag being flag + 1

                                        Block neighborBlock = world.getBlock(neighbor_x, neighbor_y, neighbor_z);
                                        opacity = Math.max(1, neighborBlock.getLightOpacity(world, neighbor_x, neighbor_y, neighbor_z));

                                        //Proceed only if the block is non-solid
                                        if (opacity < 15) {

                                            //Get Saved light value from face
                                            neighborLightEntry = world.pipe.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);

                                            int queueLightEntryFiltered = calculateOpacity(queueLightEntry, opacity, neighborBlock, world, neighbor_x, neighbor_y, neighbor_z);

                                            int queue_filtered_l = (queueLightEntryFiltered >> CLApi.bitshift_l) & 0xF;
                                            int queue_filtered_r = (queueLightEntryFiltered >> CLApi.bitshift_r) & CLApi.bitmask;
                                            int queue_filtered_g = (queueLightEntryFiltered >> CLApi.bitshift_g) & CLApi.bitmask;
                                            int queue_filtered_b = (queueLightEntryFiltered >> CLApi.bitshift_b) & CLApi.bitmask;

                                            int neighbor_l = (neighborLightEntry >> CLApi.bitshift_l) & 0xF;
                                            int neighbor_r = (neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                            int neighbor_g = (neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                            int neighbor_b = (neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;

                                            ll = queue_l > neighbor_l ? Math.max(0, queue_filtered_l) : neighbor_l;
                                            rl = queue_r > neighbor_r ? Math.max(0, queue_filtered_r) : neighbor_r;
                                            gl = queue_g > neighbor_g ? Math.max(0, queue_filtered_g) : neighbor_g;
                                            bl = queue_b > neighbor_b ? Math.max(0, queue_filtered_b) : neighbor_b;

                                            long light_combine = (ll << CLApi.bitshift_l) | (rl << CLApi.bitshift_r) | (gl << CLApi.bitshift_g) | (bl << CLApi.bitshift_b);

                                            if (((ll > neighbor_l) ||
                                                    (rl > neighbor_r) ||
                                                    (gl > neighbor_g) ||
                                                    (bl > neighbor_b)) && (getter < world.pipe.lightAdditionBlockList.length)) {
                                                world.pipe.lightAdditionNeeded[neighbor_x - par_x + offset][neighbor_y - par_y + offset][neighbor_z - par_z + offset] = world.pipe.updateFlag; // Mark neighbor to be processed
                                                world.pipe.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size) | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | (light_combine << (coord_size * 3));
                                                lightAdditionsCalled++;
                                            } else if ((queue_l + opacity < neighbor_l) ||
                                                    (queue_r + opacity < neighbor_r) ||
                                                    (queue_g + opacity < neighbor_g) ||
                                                    (queue_b + opacity < neighbor_b)) {
                                                if (Math.abs(queue_x - rel_x) < offset && Math.abs(queue_y - rel_y) < offset && Math.abs(queue_z - rel_z) < offset) {
                                                    world.pipe.lightBackfillNeeded[queue_x - rel_x + offset][queue_y - rel_y + offset][queue_z - rel_z + offset] = world.pipe.updateFlag; // Mark queue location to be re-processed
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
                CLLog.warn("Error in Light Addition:" + filler + (par1Enu==EnumSkyBlock.Block?" (isBlock)": " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue) + " Comp:" + Integer.toBinaryString((int)compLightValue) + " isBackfill:" + " updateFlag:" + world.pipe.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:" + lightAdditionsSatisfied);
            }

            if (shouldIncrement) { // Only proceed if we are NOT in a recursive call
                world.theProfiler.endStartSection("lightSubtraction");

                //Reset indexes
                filler = 0;
                getter = 0;

                if (saved_l > comp_l || saved_r > comp_r || saved_g > comp_g || saved_b > comp_b) { //savedLightValue has components that are larger than compLightValue
                    //Light Destruction

                    world.pipe.setLightValue(par1Enu, par_x, par_y, par_z, (int) compLightValue); // This kills the light
                    world.pipe.lightAdditionBlockList[getter++] = (startCoord | (savedLightValue << (coord_size * 3)));

                    while (filler <= getter) {
                        queueEntry = world.pipe.lightAdditionBlockList[filler++]; //Get Entry at l, which starts at 0
                        queue_x = ((int) (queueEntry & coord_mask) - size + par_x); //Get Entry X coord
                        queue_y = ((int) ((queueEntry >> coord_size) & coord_mask) - size + par_y); //Get Entry Y coord
                        queue_z = ((int) ((queueEntry >> (coord_size * 2)) & coord_mask) - size + par_z); //Get Entry Z coord
                        queueLightEntry = (int) (queueEntry >>> (coord_size * 3)); //Get Entry's saved Light

                        man_x = MathHelper.abs_int(queue_x - par_x);
                        man_y = MathHelper.abs_int(queue_y - par_y);
                        man_z = MathHelper.abs_int(queue_z - par_z);
                        manhattan_distance = man_x + man_y + man_z;

                        int limit_test = Math.max(Math.max(Math.max(saved_l, saved_r), saved_g), saved_b);
                        if (manhattan_distance < limit_test) { //Limits the splat size to the initial brightness value
                            for (neighborIndex = 0; neighborIndex < 6; ++neighborIndex) {
                                neighbor_x = queue_x + Facing.offsetsXForSide[neighborIndex];
                                neighbor_y = queue_y + Facing.offsetsYForSide[neighborIndex];
                                neighbor_z = queue_z + Facing.offsetsZForSide[neighborIndex];
                                if (neighbor_y < 0 || neighbor_y > 255)
                                    continue;

                                man_x = MathHelper.abs_int(neighbor_x - par_x);
                                man_y = MathHelper.abs_int(neighbor_y - par_y);
                                man_z = MathHelper.abs_int(neighbor_z - par_z);

                                opacity = Math.max(1, world.getBlock(neighbor_x, neighbor_y, neighbor_z).getLightOpacity(world, neighbor_x, neighbor_y, neighbor_z));
                                neighborLightEntry = world.pipe.getSavedLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z);

                                int queue_l = (queueLightEntry >> CLApi.bitshift_l) & 0xF;
                                int queue_r = (queueLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                int queue_g = (queueLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                int queue_b = (queueLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;

                                int neighbor_l = (neighborLightEntry >> CLApi.bitshift_l) & 0xF;
                                int neighbor_r = (neighborLightEntry >> CLApi.bitshift_r) & CLApi.bitmask;
                                int neighbor_g = (neighborLightEntry >> CLApi.bitshift_g) & CLApi.bitmask;
                                int neighbor_b = (neighborLightEntry >> CLApi.bitshift_b) & CLApi.bitmask;

                                if (opacity < 15 || neighborLightEntry > 0) {
                                    //Get Saved light value from face

                                    //   |------------------maximum theoretical light value------------------|    |------saved light value------|
                                    ll = (Math.max(queue_l - (man_x + man_y + man_z), 0) >= neighbor_l) ? 0 : neighbor_l;
                                    rl = (Math.max(queue_r - (man_x + man_y + man_z), 0) >= neighbor_r) ? 0 : neighbor_r;
                                    gl = (Math.max(queue_g - (man_x + man_y + man_z), 0) >= neighbor_g) ? 0 : neighbor_g;
                                    bl = (Math.max(queue_b - (man_x + man_y + man_z), 0) >= neighbor_b) ? 0 : neighbor_b;

                                    sortValue = 0;
                                    if ((queue_l > 0) && (ll != 0)) {
                                        sortValue = (int) ll;
                                    }
                                    if ((queue_r > 0) && (rl > sortValue)) {
                                        sortValue = (int)rl;
                                    }
                                    if ((queue_g > 0) && (gl > sortValue)) {
                                        sortValue = (int)gl;
                                    }
                                    if ((queue_b > 0) && (bl > sortValue)) {
                                        sortValue = (int)bl;
                                    }

                                    long light_combine = (ll << CLApi.bitshift_l) | (rl << CLApi.bitshift_r) | (gl << CLApi.bitshift_g) | (bl << CLApi.bitshift_b);
                                    //If the light we are looking at on the edge is brighter or equal to the current light in any way, then there must be a light over there that's doing it, so we'll stop eating colors and lights in that direction
                                    if (neighborLightEntry != light_combine) {

                                        if (sortValue != 0) {
                                            if (ll == sortValue) {
                                                ll = 0;
                                            }
                                            if (rl == sortValue) {
                                                rl = 0;
                                            }
                                            if (gl == sortValue) {
                                                gl = 0;
                                            }
                                            if (bl == sortValue) {
                                                bl = 0;
                                            }
                                            light_combine = (ll << CLApi.bitshift_l) | (rl << CLApi.bitshift_r) | (gl << CLApi.bitshift_g) | (bl << CLApi.bitshift_b);

                                            world.pipe.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = world.pipe.updateFlag;
                                            world.pipe.lightBackfillBlockList[sortValue - 1][world.pipe.lightBackfillIndexes[sortValue - 1]++] = (neighbor_x - par_x + size) | ((neighbor_y - par_y + size) << (coord_size * 1)) | ((neighbor_z - par_z + size) << (coord_size * 2)); //record coordinates for backfill
                                        }

                                        world.pipe.setLightValue(par1Enu, neighbor_x, neighbor_y, neighbor_z, (int)light_combine); // This kills the light
                                        world.pipe.lightAdditionBlockList[getter++] = ((long) neighbor_x - (long) par_x + size) | (((long) neighbor_y - (long) par_y + size) << coord_size) | (((long) neighbor_z - (long) par_z + size) << (coord_size * 2)) | ((long) queueLightEntry << (coord_size * 3)); //this array keeps the algorithm going, don't touch
                                    } else {
                                        if (sortValue != 0) {
                                            world.pipe.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] = world.pipe.updateFlag;
                                            world.pipe.lightBackfillBlockList[sortValue - 1][world.pipe.lightBackfillIndexes[sortValue - 1]++] = (queue_x - par_x + size) | ((queue_y - par_y + size) << coord_size) | ((queue_z - par_z + size) << (coord_size * 2)); //record coordinates for backfill
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (filler > 4097 * 2) {
                        CLLog.warn("Light Subtraction Overfilled:" + filler + (par1Enu == EnumSkyBlock.Block ? " (isBlock)" : " (isSky)") + " Saved:" + Integer.toBinaryString((int) savedLightValue) + " Comp:" + Integer.toBinaryString((int) compLightValue) + " isBackfill:" + " updateFlag:" + world.pipe.updateFlag + " Called:" + lightAdditionsCalled + " Satisfied:" + lightAdditionsSatisfied);
                    }

                    world.theProfiler.endStartSection("lightBackfill");

                    //Backfill
                    for (filler = world.pipe.lightBackfillIndexes.length - 1; filler >= 0; filler--) {
                        while (world.pipe.lightBackfillIndexes[filler] > 0) {
                            getter = world.pipe.lightBackfillBlockList[filler][--world.pipe.lightBackfillIndexes[filler]];
                            queue_x = (getter & coord_mask) - size + par_x; //Get Entry X coord
                            queue_y = (getter >> (coord_size * 1) & coord_mask) - size + par_y; //Get Entry Y coord
                            queue_z = (getter >> (coord_size * 2) & coord_mask) - size + par_z; //Get Entry Z coord

                            if (world.pipe.lightBackfillNeeded[queue_x - par_x + offset][queue_y - par_y + offset][queue_z - par_z + offset] == world.pipe.updateFlag) {
                                CLWorldHelper.updateLightByType_withIncrement(world, par1Enu, queue_x, queue_y, queue_z, false, rel_x, rel_y, rel_z); ///oooooOOOOoooo spoooky!
                            }
                        }
                    }
                }
            }

            world.theProfiler.endSection();
            return true;
        }
    }

    /**
     * Patching in Dynamic Lights Compatibility
     */
    private static int getLightValueSomehow(Block block, World world, int par_x, int par_y, int par_z) {
        if (ColoredLightsCoreDummyContainer.getDynamicLight != null && world.isRemote) {
                nop();
            try {
                int a = (Integer)ColoredLightsCoreDummyContainer.getDynamicLight.invoke(null, world, block, par_x, par_y, par_z);
                if (a != 0) CLLog.info("got :" + a);
                return a;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return block.getLightValue(world, par_x, par_y, par_z);
    }

    //TODO: Remove nop()
    private static void nop() {
        return;
    }
}