package com.lightingoverhaul.mixinmod.mixins;

import org.spongepowered.asm.mixin.Mixin;

import com.lightingoverhaul.mixinmod.helper.RGBHelper;

import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkCache.class)
public abstract class ChunkCacheMixin {
    @Inject(at = @At(value = "HEAD"), method = "getLightBrightnessForSkyBlocks", cancellable = true)
    public void getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(RGBHelper.getLightBrightnessForSkyBlocks(((ChunkCache)(Object)this), x, y, z, lightValue));
    }
}
