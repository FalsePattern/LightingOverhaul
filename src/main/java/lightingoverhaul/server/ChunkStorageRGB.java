package lightingoverhaul.server;

import lightingoverhaul.mixin.interfaces.IExtendedBlockStorageMixin;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import static lightingoverhaul.LightingOverhaul.LOlog;

/**
 * Methods for loading/saving RGB data to/from world save
 * 
 * @author heaton84
 */
public class ChunkStorageRGB {

    /**
     * Constructs a NibbleArray from a raw stream of byte data. If the byte data is
     * incomplete, returns an empty array.
     * 
     * @param rawdata The raw bytestream to build an array from.
     * @return Instance of a NibbleArray.
     */
    public static NibbleArray checkedGetNibbleArray(byte[] rawdata) {
        if (rawdata.length == 0) {
            return new NibbleArray(4096, 4);
        } else if (rawdata.length < 2048) {
            LOlog.warn("checkedGetNibbleArray: rawdata is too short: {}, expected 2048", rawdata.length);
            return new NibbleArray(4096, 4);
        } else
            return new NibbleArray(rawdata, 4);
    }

    /**
     * Loads RGB color data from a world store, if present.
     * 
     * @param chunk The chunk to populate with data
     * @param data  Top-level NBTTag, must contain "Level" tag.
     * @return true if color data was loaded, false if not present or an error was
     *         encountered
     */
    public static boolean loadColorData(Chunk chunk, NBTTagCompound data) {
        NibbleArray rColorArray;
        NibbleArray gColorArray;
        NibbleArray bColorArray;
        NibbleArray rColorArraySun;
        NibbleArray gColorArraySun;
        NibbleArray bColorArraySun;
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NBTTagCompound level = data.getCompoundTag("Level");
        NBTTagList nbttaglist = level.getTagList("Sections", 10);
        boolean foundColorData = false;

        for (int k = 0; k < nbttaglist.tagCount(); ++k) {
            NBTTagCompound nbtYCompound = nbttaglist.getCompoundTagAt(k);

            if (chunkStorageArrays[k] != null) {
                if (nbtYCompound.hasKey("RedColorArray")) // , 7))
                {
                    rColorArray =    checkedGetNibbleArray(nbtYCompound.getByteArray("RedColorArray"));
                    gColorArray =    checkedGetNibbleArray(nbtYCompound.getByteArray("GreenColorArray"));
                    bColorArray =    checkedGetNibbleArray(nbtYCompound.getByteArray("BlueColorArray"));
                    rColorArraySun = checkedGetNibbleArray(nbtYCompound.getByteArray("RedColorArraySun"));
                    gColorArraySun = checkedGetNibbleArray(nbtYCompound.getByteArray("GreenColorArraySun"));
                    bColorArraySun = checkedGetNibbleArray(nbtYCompound.getByteArray("BlueColorArraySun"));

                    // Set color arrays on chunk.storageArrays

                    IExtendedBlockStorageMixin blockStorageMixin = (IExtendedBlockStorageMixin) chunkStorageArrays[k];

                    blockStorageMixin.setRedColorArray(rColorArray);
                    blockStorageMixin.setGreenColorArray(gColorArray);
                    blockStorageMixin.setBlueColorArray(bColorArray);
                    blockStorageMixin.setRedColorArraySun(rColorArraySun);
                    blockStorageMixin.setGreenColorArraySun(gColorArraySun);
                    blockStorageMixin.setBlueColorArraySun(bColorArraySun);

                    foundColorData = true;

                }
                else
                LOlog.warn("NO NIBBLE ARRAY EXISTS FOR {} {} {}", chunk.xPosition,
                chunk.zPosition, k);
            }
        }

        return foundColorData;
    }

    /**
     * Loads RGB color data from a world store, if present.
     * 
     * @param chunk The chunk to populate with data
     * @return true if color data was loaded, false if not present or an error was
     *         encountered
     */
    public static boolean loadColorData(Chunk chunk, int arraySize, int[] yLocation, NibbleArray[] redColorData, NibbleArray[] greenColorData, NibbleArray[] blueColorData, NibbleArray[] redColorDataSun, NibbleArray[] greenColorDataSun, NibbleArray[] blueColorDataSun) {
        NibbleArray rColorArray;
        NibbleArray gColorArray;
        NibbleArray bColorArray;
        NibbleArray rColorArraySun;
        NibbleArray gColorArraySun;
        NibbleArray bColorArraySun;
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        boolean foundColorData = false;

        for (int k = 0; k < arraySize; ++k) {
            if (chunkStorageArrays[k] != null) {
                if (chunkStorageArrays[k].getYLocation() != yLocation[k])
                    LOlog.error("EBS DATA OUT OF SEQUENCE. Expected {}, got {}", chunkStorageArrays[k].getYLocation(), yLocation[k]);

                rColorArray = redColorData[k];
                gColorArray = greenColorData[k];
                bColorArray = blueColorData[k];
                rColorArraySun = redColorDataSun[k];
                gColorArraySun = greenColorDataSun[k];
                bColorArraySun = blueColorDataSun[k];

                // Set color arrays on chunk.storageArrays
                IExtendedBlockStorageMixin blockStorageMixin = (IExtendedBlockStorageMixin) chunkStorageArrays[k];

                blockStorageMixin.setRedColorArray(rColorArray);
                blockStorageMixin.setGreenColorArray(gColorArray);
                blockStorageMixin.setBlueColorArray(bColorArray);
                blockStorageMixin.setRedColorArraySun(rColorArraySun);
                blockStorageMixin.setGreenColorArraySun(gColorArraySun);
                blockStorageMixin.setBlueColorArraySun(bColorArraySun);

                foundColorData = true;

            }
        }

        return foundColorData;
    }

    /**
     * Saves RGB color data into world store NBTTag data. Should be called before
     * chunk is saved.
     * 
     * @param chunk The chunk to extract RGB color data from.
     * @param data  Top-level NBTTag, must contain "Level" tag.
     * @return true if color data was saved, false if an error was encountered
     */
    public static boolean saveColorData(Chunk chunk, NBTTagCompound data) {
        NibbleArray rColorArray;
        NibbleArray gColorArray;
        NibbleArray bColorArray;
        NibbleArray rColorArraySun;
        NibbleArray gColorArraySun;
        NibbleArray bColorArraySun;
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NBTTagCompound level = data.getCompoundTag("Level");
        NBTTagList nbttaglist = level.getTagList("Sections", 10);

        for (int k = 0; k < chunkStorageArrays.length; k++) {
            if (chunkStorageArrays[k] != null) {
                NBTTagCompound nbtYCompound = nbttaglist.getCompoundTagAt(k);
                IExtendedBlockStorageMixin blockStorageMixin = (IExtendedBlockStorageMixin) chunkStorageArrays[k];

                // Add our RGB arrays to it
                rColorArray = blockStorageMixin.getRedColorArray();
                gColorArray = blockStorageMixin.getGreenColorArray();
                bColorArray = blockStorageMixin.getBlueColorArray();
                rColorArraySun = blockStorageMixin.getRedColorArraySun();
                gColorArraySun = blockStorageMixin.getGreenColorArraySun();
                bColorArraySun = blockStorageMixin.getBlueColorArraySun();

                // Cauldron adds .getValueArray() instead of .data
                if (FMLCommonHandler.instance().getModName().contains("cauldron")) {
                    /*
                     * nbtYCompound.setByteArray("RedColorArray", rColorArray.getValueArray());
                     * nbtYCompound.setByteArray("GreenColorArray", gColorArray.getValueArray());
                     * nbtYCompound.setByteArray("BlueColorArray", bColorArray.getValueArray());
                     * nbtYCompound.setByteArray("RedColorArray2", rColorArray2.getValueArray());
                     * nbtYCompound.setByteArray("GreenColorArray2", gColorArray2.getValueArray());
                     * nbtYCompound.setByteArray("BlueColorArray2", bColorArray2.getValueArray());
                     * nbtYCompound.setByteArray("RedColorArraySun",
                     * rColorArraySun.getValueArray());
                     * nbtYCompound.setByteArray("GreenColorArraySun",
                     * gColorArraySun.getValueArray());
                     * nbtYCompound.setByteArray("BlueColorArraySun",
                     * bColorArraySun.getValueArray());
                     */
                } else {
                    nbtYCompound.setByteArray("RedColorArray", rColorArray.data);
                    nbtYCompound.setByteArray("GreenColorArray", gColorArray.data);
                    nbtYCompound.setByteArray("BlueColorArray", bColorArray.data);
                    nbtYCompound.setByteArray("RedColorArraySun", rColorArraySun.data);
                    nbtYCompound.setByteArray("GreenColorArraySun", gColorArraySun.data);
                    nbtYCompound.setByteArray("BlueColorArraySun", bColorArraySun.data);
                }
            }
        }

        return true;
    }

    /**
     * Extracts all the red color arrays from a chunk's extended block storage
     * 
     * @param chunk
     * @return An array of NibbleArrays containing red color data for the chunk
     */
    public static NibbleArray[] getRedColorArrays(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] redColorArrays;

        redColorArrays = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                redColorArrays[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getRedColorArray();
            else
                redColorArrays[i] = null;
        }

        return redColorArrays;
    }

    public static NibbleArray[] getRedColorArraysSun(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] redColorArraysSun;

        redColorArraysSun = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                redColorArraysSun[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getRedColorArraySun();
            else
                redColorArraysSun[i] = null;
        }

        return redColorArraysSun;
    }

    /**
     * Extracts all the green color arrays from a chunk's extended block storage
     * 
     * @param chunk
     * @return An array of NibbleArrays containing green color data for the chunk
     */

    public static NibbleArray[] getGreenColorArrays(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] greenColorArrays;

        greenColorArrays = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                greenColorArrays[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getGreenColorArray();
            else
                greenColorArrays[i] = null;
        }

        return greenColorArrays;
    }

    public static NibbleArray[] getGreenColorArraysSun(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] greenColorArraysSun;

        greenColorArraysSun = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                greenColorArraysSun[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getGreenColorArraySun();
            else
                greenColorArraysSun[i] = null;
        }

        return greenColorArraysSun;
    }

    /**
     * Extracts all the blue color arrays from a chunk's extended block storage
     * 
     * @param chunk
     * @return An array of NibbleArrays containing blue color data for the chunk
     */

    public static NibbleArray[] getBlueColorArrays(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] blueColorArrays;

        blueColorArrays = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                blueColorArrays[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getBlueColorArray();
            else
                blueColorArrays[i] = null;
        }

        return blueColorArrays;
    }

    public static NibbleArray[] getBlueColorArraysSun(Chunk chunk) {
        ExtendedBlockStorage[] chunkStorageArrays = chunk.getBlockStorageArray();
        NibbleArray[] blueColorArraysSun;

        blueColorArraysSun = new NibbleArray[chunkStorageArrays.length];

        for (int i = 0; i < chunkStorageArrays.length; i++) {
            if (chunkStorageArrays[i] != null)
                blueColorArraysSun[i] = ((IExtendedBlockStorageMixin) chunkStorageArrays[i]).getBlueColorArraySun();
            else
                blueColorArraysSun[i] = null;
        }

        return blueColorArraysSun;
    }

    public static int[] getYLocationArray(Chunk chunk) {
        ExtendedBlockStorage[] ebs = chunk.getBlockStorageArray();
        int[] y = new int[ebs.length];

        for (int i = 0; i < ebs.length; i++) {
            if (ebs[i] == null)
                y[i] = -1;
            else
                y[i] = ebs[i].getYLocation();
        }

        return y;
    }
}
