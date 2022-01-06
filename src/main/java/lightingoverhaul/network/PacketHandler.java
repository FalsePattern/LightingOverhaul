package lightingoverhaul.network;

import lightingoverhaul.ModInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import lightingoverhaul.server.ChunkStorageRGB;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import static lightingoverhaul.LightingOverhaul.LOlog;

public class PacketHandler {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.MODID + "core");

    public static void init() {
        INSTANCE.registerMessage(ChunkColorDataPacket.class, ChunkColorDataPacket.class, 0, Side.SERVER);
        INSTANCE.registerMessage(ChunkColorDataPacket.class, ChunkColorDataPacket.class, 0, Side.CLIENT);
    }

    public static void sendChunkColorData(Chunk chunk, EntityPlayerMP player) {
        try {
            ChunkColorDataPacket packet = new ChunkColorDataPacket();
            NibbleArray[] redColorArray = ChunkStorageRGB.getRedColorArrays(chunk);
            NibbleArray[] greenColorArray = ChunkStorageRGB.getGreenColorArrays(chunk);
            NibbleArray[] blueColorArray = ChunkStorageRGB.getBlueColorArrays(chunk);
            NibbleArray[] redColorArraySun = ChunkStorageRGB.getRedColorArraysSun(chunk);
            NibbleArray[] greenColorArraySun = ChunkStorageRGB.getGreenColorArraysSun(chunk);
            NibbleArray[] blueColorArraySun = ChunkStorageRGB.getBlueColorArraysSun(chunk);

            packet.chunkXPosition = chunk.xPosition;
            packet.chunkZPosition = chunk.zPosition;
            packet.arraySize = redColorArray.length;
            packet.yLocation = ChunkStorageRGB.getYLocationArray(chunk);
            packet.RedColorArray = redColorArray;
            packet.GreenColorArray = greenColorArray;
            packet.BlueColorArray = blueColorArray;
            packet.RedColorArraySun = redColorArraySun;
            packet.GreenColorArraySun = greenColorArraySun;
            packet.BlueColorArraySun = blueColorArraySun;

            // this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            // this.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
            // this.channels.get(Side.SERVER).writeOutbound(packet);

            // Think this is right
            INSTANCE.sendTo(packet, player);

        } catch (Exception e) {
            LOlog.warn("SendChunkColorData()  ", e);
        }

    }

}
