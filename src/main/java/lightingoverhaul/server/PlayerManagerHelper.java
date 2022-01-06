package lightingoverhaul.server;

import java.util.List;

import lightingoverhaul.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;

import static lightingoverhaul.LightingOverhaul.LOlog;

public class PlayerManagerHelper {

    public PlayerManagerHelper() {
    }

    /**
     * Invoked for each player in
     * net.minecraft.server.management.PlayerManager.sendToAllPlayersWatchingChunk
     * 
     * Happens when a server is sending chunk data to a player
     * 
     * @param player
     * @param chunkLocation
     */
    public static void sendToPlayerWatchingChunk(EntityPlayerMP player, ChunkCoordIntPair chunkLocation) {
        // LOlog.info("Server just sent chunk ({}, {}) to player {}",
        // chunkLocation.chunkXPos, chunkLocation.chunkZPos, player.getDisplayName());

        // TODO: Load chunk from server
        // sendChunkRGBDataToPlayer(player, chunkLocation.chunkXPos,
        // chunkLocation.chunkZPos, null);
    }

    public static void entityPlayerMP_onUpdate(List<Chunk> chunks, EntityPlayerMP player) {
        for (Chunk c : chunks) {
            //LOlog.info("S26: Server just sent chunk ({}, {}) to player {}", c.xPosition,
            //c.zPosition, player.getDisplayName());

            sendChunkRGBDataToPlayer(player, c.xPosition, c.zPosition, c);
        }
    }

    public static void sendChunkRGBDataToPlayer(EntityPlayerMP player, int chunkX, int chunkZ, Chunk chunk) {
        if (chunk == null) {
            chunk = player.worldObj.getChunkFromChunkCoords(chunkX, chunkZ);

            if (chunk == null) {
                LOlog.warn("Could not load chunk ({}, {}) for RGB color data!", chunkX, chunkZ);
                return;
            }
        }

        PacketHandler.sendChunkColorData(chunk, player);
    }
}
