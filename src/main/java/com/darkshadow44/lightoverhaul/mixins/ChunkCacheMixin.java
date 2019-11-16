package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.darkshadow44.lightoverhaul.helper.ChunkCacheHelper;

import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;

@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin {
    @Shadow
    public int getSkyBlockTypeBrightness(EnumSkyBlock paramEnumSkyBlock, int x, int y, int z) {
        return 0;
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue) {
        return ChunkCacheHelper.getLightBrightnessForSkyBlocks((ChunkCache) (Object) this, x, y, z, lightValue);
    }
}
