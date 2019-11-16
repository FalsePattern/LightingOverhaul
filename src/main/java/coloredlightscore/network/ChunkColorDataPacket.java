package coloredlightscore.network;

import static coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin.CLLog;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import coloredlightscore.server.ChunkStorageRGB;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;

public class ChunkColorDataPacket implements IMessage, IMessageHandler<ChunkColorDataPacket, IMessage> {

    // In order of packet layout:
    // public int packetId;
    public int chunkXPosition;
    public int chunkZPosition;
    public int arraySize;

    // The following are stored as raw byte data:
    public NibbleArray[] RedColorArray;
    public NibbleArray[] GreenColorArray;
    public NibbleArray[] BlueColorArray;
    public NibbleArray[] RedColorArray2;
    public NibbleArray[] GreenColorArray2;
    public NibbleArray[] BlueColorArray2;
    public NibbleArray[] RedColorArraySun;
    public NibbleArray[] GreenColorArraySun;
    public NibbleArray[] BlueColorArraySun;
    public int[] yLocation;

    private final boolean USE_COMPRESSION = true;

    @Override
    public IMessage onMessage(ChunkColorDataPacket packet, MessageContext context) {
        if (context.side == Side.CLIENT)
            processColorDataPacket(packet);

        return null;
    }

    @SideOnly(Side.CLIENT)
    private void processColorDataPacket(ChunkColorDataPacket packet) {
        ChunkColorDataPacket ccdPacket = (ChunkColorDataPacket) packet;
        Chunk targetChunk = null;

        targetChunk = Minecraft.getMinecraft().theWorld.getChunkFromChunkCoords(ccdPacket.chunkXPosition, ccdPacket.chunkZPosition);

        if (targetChunk != null) {
            ChunkStorageRGB.loadColorData(targetChunk, ccdPacket.arraySize, ccdPacket.yLocation, ccdPacket.RedColorArray, ccdPacket.GreenColorArray, ccdPacket.BlueColorArray, ccdPacket.RedColorArray2,
                    ccdPacket.GreenColorArray2, ccdPacket.BlueColorArray2, ccdPacket.RedColorArraySun, ccdPacket.GreenColorArraySun, ccdPacket.BlueColorArraySun);
            // CLLog.info("ProcessColorDataPacket() loaded RGB for ({},{})",
            // ccdPacket.chunkXPosition, ccdPacket.chunkZPosition);
        } else
            CLLog.warn("ProcessColorDataPacket()  Chunk located at ({}, {}) could not be found in the local world!", ccdPacket.chunkXPosition, ccdPacket.chunkZPosition);
    }

    @Override
    public void fromBytes(ByteBuf bytes) {
        try {
            byte[] rawColorData = new byte[2048 * 16 * 3 * 2];
            byte[] compressedColorData = new byte[32000];
            byte[] nibbleData = new byte[2048];
            int compressedSize;
            int arraysPresent;
            int p = 0;

            chunkXPosition = bytes.readInt();
            chunkZPosition = bytes.readInt();
            arraySize = bytes.readInt();

            yLocation = new int[arraySize];

            for (int i = 0; i < arraySize; i++)
                yLocation[i] = bytes.readInt();

            arraysPresent = bytes.readInt();

            RedColorArray = new NibbleArray[arraySize];
            GreenColorArray = new NibbleArray[arraySize];
            BlueColorArray = new NibbleArray[arraySize];
            RedColorArray2 = new NibbleArray[arraySize];
            GreenColorArray2 = new NibbleArray[arraySize];
            BlueColorArray2 = new NibbleArray[arraySize];
            RedColorArraySun = new NibbleArray[arraySize];
            GreenColorArraySun = new NibbleArray[arraySize];
            BlueColorArraySun = new NibbleArray[arraySize];

            if (USE_COMPRESSION) {
                compressedSize = bytes.readInt();
                bytes.readBytes(compressedColorData, 0, compressedSize);

                Inflater inflater = new Inflater();
                inflater.setInput(compressedColorData);

                try {
                    inflater.inflate(rawColorData);
                } catch (DataFormatException e) {
                    CLLog.warn("ChunkColorDataPacket()  ", e);
                } finally {
                    inflater.end();
                }
            } else
                // !USE_COMPRESSION
                bytes.readBytes(rawColorData);

            for (int i = 0; i < arraySize; i++) {
                if ((arraysPresent & (1 << i)) != 0) {
                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    RedColorArray[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    GreenColorArray[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    BlueColorArray[i] = new NibbleArray(nibbleData, 4); // 4,59,10 y:3[11] cx:-16 cz:10

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    RedColorArray2[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    GreenColorArray2[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    BlueColorArray2[i] = new NibbleArray(nibbleData, 4); // 4,59,10 y:3[11] cx:-16 cz:10

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    RedColorArraySun[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    GreenColorArraySun[i] = new NibbleArray(nibbleData, 4);

                    p += 2048;

                    nibbleData = new byte[2048];
                    System.arraycopy(rawColorData, p, nibbleData, 0, 2048);
                    BlueColorArraySun[i] = new NibbleArray(nibbleData, 4); // 4,59,10 y:3[11] cx:-16 cz:10

                    p += 2048;

                } else {
                    RedColorArray[i] = new NibbleArray(4096, 4);
                    GreenColorArray[i] = new NibbleArray(4096, 4);
                    BlueColorArray[i] = new NibbleArray(4096, 4);
                    RedColorArray2[i] = new NibbleArray(4096, 4);
                    GreenColorArray2[i] = new NibbleArray(4096, 4);
                    BlueColorArray2[i] = new NibbleArray(4096, 4);
                }
            }

        } catch (Exception e) {
            CLLog.error("fromBytes ", e);
        }
    }

    @Override
    public void toBytes(ByteBuf bytes) {
        try {
            byte[] rawColorData = new byte[2048 * 16 * 3 * 2];
            byte[] compressedColorData = new byte[32000];
            int compressedSize;
            int arraysPresent = 0;
            int p = 0;

            bytes.writeInt(chunkXPosition);
            bytes.writeInt(chunkZPosition);
            bytes.writeInt(arraySize);

            // Crank out nibble arrays
            for (int i = 0; i < arraySize; i++) {
                if (RedColorArray[i] != null || GreenColorArray[i] != null || BlueColorArray[i] != null) {
                    arraysPresent |= (1 << i);
                    if (FMLCommonHandler.instance().getModName().contains("cauldron")) {
                        /*
                         * byte[] localRed = RedColorArray[i].getValueArray(); byte[] localGreen =
                         * GreenColorArray[i].getValueArray(); byte[] localBlue =
                         * BlueColorArray[i].getValueArray(); byte[] localRed2 =
                         * RedColorArray2[i].getValueArray(); byte[] localGreen2 =
                         * GreenColorArray2[i].getValueArray(); byte[] localBlue2 =
                         * BlueColorArray2[i].getValueArray(); byte[] localRedSun =
                         * RedColorArraySun[i].getValueArray(); byte[] localGreenSun =
                         * GreenColorArraySun[i].getValueArray(); byte[] localBlueSun =
                         * BlueColorArraySun[i].getValueArray(); System.arraycopy(localRed, 0,
                         * rawColorData, p, localRed.length); p += localRed.length;
                         * System.arraycopy(localGreen, 0, rawColorData, p, localGreen.length); p +=
                         * localGreen.length; System.arraycopy(localBlue, 0, rawColorData, p,
                         * localBlue.length); p += localBlue.length; System.arraycopy(localRed2, 0,
                         * rawColorData, p, localRed2.length); p += localRed2.length;
                         * System.arraycopy(localGreen2, 0, rawColorData, p, localGreen2.length); p +=
                         * localGreen2.length; System.arraycopy(localBlue2, 0, rawColorData, p,
                         * localBlue2.length); p += localBlue2.length; System.arraycopy(localRedSun, 0,
                         * rawColorData, p, localRedSun.length); p += localRedSun.length;
                         * System.arraycopy(localGreenSun, 0, rawColorData, p, localGreenSun.length); p
                         * += localGreenSun.length; System.arraycopy(localBlueSun, 0, rawColorData, p,
                         * localBlueSun.length); p += localBlueSun.length;
                         */
                    } else {
                        System.arraycopy(RedColorArray[i].data, 0, rawColorData, p, RedColorArray[i].data.length);
                        p += RedColorArray[i].data.length;
                        System.arraycopy(GreenColorArray[i].data, 0, rawColorData, p, GreenColorArray[i].data.length);
                        p += GreenColorArray[i].data.length;
                        System.arraycopy(BlueColorArray[i].data, 0, rawColorData, p, BlueColorArray[i].data.length);
                        p += BlueColorArray[i].data.length;
                        System.arraycopy(RedColorArray2[i].data, 0, rawColorData, p, RedColorArray2[i].data.length);
                        p += RedColorArray2[i].data.length;
                        System.arraycopy(GreenColorArray2[i].data, 0, rawColorData, p, GreenColorArray2[i].data.length);
                        p += GreenColorArray2[i].data.length;
                        System.arraycopy(BlueColorArray2[i].data, 0, rawColorData, p, BlueColorArray2[i].data.length);
                        p += BlueColorArray2[i].data.length;
                        System.arraycopy(RedColorArraySun[i].data, 0, rawColorData, p, RedColorArraySun[i].data.length);
                        p += RedColorArraySun[i].data.length;
                        System.arraycopy(GreenColorArraySun[i].data, 0, rawColorData, p, GreenColorArraySun[i].data.length);
                        p += GreenColorArraySun[i].data.length;
                        System.arraycopy(BlueColorArraySun[i].data, 0, rawColorData, p, BlueColorArraySun[i].data.length);
                        p += BlueColorArraySun[i].data.length;
                    }
                }

                // Add Y location
                bytes.writeInt(yLocation[i]);
            }

            bytes.writeInt(arraysPresent);

            if (USE_COMPRESSION) {
                Deflater deflate = new Deflater(-1);
                deflate.setInput(rawColorData);
                deflate.finish();

                compressedSize = deflate.deflate(compressedColorData);

                if (compressedSize == 0)
                    CLLog.warn("writePacket compression failed");

                bytes.writeInt(compressedSize);
                bytes.writeBytes(compressedColorData, 0, compressedSize);
            } else
                // !USE_COMPRESSION
                bytes.writeBytes(rawColorData, 0, rawColorData.length);
        } catch (Exception e) {
            CLLog.error("toBytes  ", e);
        }
    }

}
