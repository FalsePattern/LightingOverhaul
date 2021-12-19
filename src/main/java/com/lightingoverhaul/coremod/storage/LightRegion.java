package com.lightingoverhaul.coremod.storage;

import lombok.Cleanup;
import lombok.val;
import lombok.var;

import java.io.*;
import java.util.zip.*;

public class LightRegion {
    private static final int CHUNKS_PER_AXIS = 32;
    private static final int CHUNKS_PER_REGION = CHUNKS_PER_AXIS * CHUNKS_PER_AXIS;
    private final LightChunk[] chunks = new LightChunk[CHUNKS_PER_REGION];
    private final LightWorld world;
    private final int x;
    private final int z;
    private final File file;
    public LightRegion(int x, int z, LightWorld world) {
        this.world = world;
        this.x = x;
        this.z = z;
        if (!world.isRemote) {
            file = new File(world.getSaveDir(), "r" + x + "," + z + ".rgb");
        } else {
            file = null;
        }
    }
    public LightChunk getChunk(int x, int z) {
        var chunk = chunks[z * CHUNKS_PER_AXIS + x];
        if (chunk == null) {
            chunk = new LightChunk();
            chunks[z * CHUNKS_PER_AXIS + x] = chunk;
        }

        return chunk;
    }

    public void save() throws IOException {
        if (world.isRemote) throw new IllegalStateException("Tried to load remote region from file!");
        val deflater = new Deflater(9, false);
        @Cleanup val out = new DataOutputStream(new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(file)), deflater));
        for (int i = 0; i < CHUNKS_PER_REGION; i++) {
            val chunk = chunks[i];
            if (chunk == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                chunk.save(out);
            }
        }
    }

    public void load() throws IOException {
        if (world.isRemote) throw new IllegalStateException("Tried to save remote region to file!");
        val inflater = new Inflater(false);
        @Cleanup val in = new DataInputStream(new InflaterInputStream(new BufferedInputStream(new FileInputStream(file)), inflater));
        for (int i = 0; i < CHUNKS_PER_REGION; i++) {
            if (in.readBoolean()) {
                var chunk = chunks[i];
                if (chunk == null) {
                    chunk = new LightChunk();
                    chunks[i] = chunk;
                }
                chunk.load(in);
            }
        }
    }


}
