package com.lightingoverhaul.coremod.fmlevents;

import static com.lightingoverhaul.coremod.asm.CoreLoadingPlugin.CLLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import com.lightingoverhaul.coremod.server.ChunkStorageRGB;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ChunkDataEventHandler {

    public ChunkDataEventHandler() {
    }

    @SubscribeEvent
    public void LoadChunk(ChunkDataEvent.Load event) {
        Chunk chunk = event.getChunk();
        NBTTagCompound data = event.getData();

        if (!ChunkStorageRGB.loadColorData(chunk, data)) {
            CLLog.warn("Failed to load color data for chunk at ({}, {})",
            chunk.xPosition, chunk.zPosition);
        }
    }

    @SubscribeEvent
    public void SaveChunk(ChunkDataEvent.Save event) {
        Chunk chunk = event.getChunk();
        NBTTagCompound data = event.getData();

        if (!ChunkStorageRGB.saveColorData(chunk, data)) {
            CLLog.warn("Failed to save color data for chunk at ({}, {})", chunk.xPosition, chunk.zPosition);
        }
    }

    @SubscribeEvent
    public void UnloadChunk(ChunkWatchEvent.UnWatch event) {

    }

}
