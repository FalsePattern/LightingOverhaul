package lightingoverhaul.fmlevents;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import lightingoverhaul.server.ChunkStorageRGB;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import static lightingoverhaul.LightingOverhaul.LOlog;

public class ChunkDataEventHandler {

    public ChunkDataEventHandler() {
    }

    @SubscribeEvent
    public void LoadChunk(ChunkDataEvent.Load event) {
        Chunk chunk = event.getChunk();
        NBTTagCompound data = event.getData();

        if (!ChunkStorageRGB.loadColorData(chunk, data)) {
            chunk.setChunkModified();
            LOlog.info("Updated chunk at ({}, {}) from vanilla to RGB",
            chunk.xPosition, chunk.zPosition);
        }
    }

    @SubscribeEvent
    public void SaveChunk(ChunkDataEvent.Save event) {
        Chunk chunk = event.getChunk();
        NBTTagCompound data = event.getData();

        if (!ChunkStorageRGB.saveColorData(chunk, data)) {
            LOlog.warn("Failed to save color data for chunk at ({}, {})", chunk.xPosition, chunk.zPosition);
        }
    }

    @SubscribeEvent
    public void UnloadChunk(ChunkWatchEvent.UnWatch event) {

    }

}
