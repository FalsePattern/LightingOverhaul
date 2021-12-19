package com.lightingoverhaul.coremod.storage;

import com.lightingoverhaul.Tags;
import com.lightingoverhaul.coremod.asm.CoreLoadingPlugin;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import lombok.Getter;
import lombok.val;
import lombok.var;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.IOException;

public class LightWorld {
    private final TLongObjectMap<LightRegion> regions;
    private final File saveDir;
    public final boolean isRemote;
    public LightWorld(World world) {
        isRemote = world.isRemote;
        if (isRemote) {
            //Do not save to disk if we're running a client on a server
            saveDir = null;
        } else {
            //Get the save directory for the current dimension
            var folder = world.provider.getSaveFolder();
            if (folder == null || "".equals(folder)) folder = "DIM0";
            saveDir = new File(new File(DimensionManager.getCurrentSaveRootDirectory(), Tags.MODID), folder);
            try {
                if (!saveDir.mkdirs()) {
                    CoreLoadingPlugin.CLLog.error("Failed to create lighting save directory for world " + saveDir + "!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        regions = new TLongObjectHashMap<>();
    }

    File getSaveDir() {
        if (isRemote) throw new IllegalStateException("Tried to get save dir for remote world!");
        return saveDir;
    }

    private LightRegion getRegion(int x, int z) throws IOException {
        val id = coordsToID(x, z);
        var region = regions.get(id);
        if (region == null) {
            region = new LightRegion(x, z, this);
            if (!isRemote) {
                //Only load from disk if we're the server, otherwise it will be done through network packets later
                region.load();
            }
        }
        return region;
    }

    public LightChunk getChunk(int x, int z) throws IOException {
        val region = getRegion(x >>> 5, z >>> 5);
        return region.getChunk(x & 0x1f, z & 0x1f);
    }

    private static long coordsToID(int x, int z) {
        return (Integer.toUnsignedLong(x) << 32) | Integer.toUnsignedLong(z);
    }
}
