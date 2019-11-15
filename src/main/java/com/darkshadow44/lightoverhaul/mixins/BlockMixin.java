package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.darkshadow44.lightoverhaul.helper.BlockHelper;

import coloredlightscore.src.api.CLApi;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Shadow
    protected int lightValue;

    @Inject(at = @At("RETURN"), method = { "setLightLevel" })
    public void setLightLevel(float par1, CallbackInfoReturnable callback) {
        // Clamp negative values
        if (par1 < 0.0F) {
            par1 = 0.0F;
        }

        if (par1 < 1.0F) {
            // If the incoming light value is a plain white call, then "color" the light
            // value white
            this.lightValue = CLApi.makeRGBLightValue(par1, par1, par1);
        } else {
            // Otherwise, let whatever it is through
            this.lightValue = (int) (15.0F * par1);
        }
    }

    @Overwrite
    public int getMixedBrightnessForBlock(IBlockAccess blockAccess, int x, int y, int z) {
        return BlockHelper.getMixedBrightnessForBlockWithColor(blockAccess, x, y, z);
    }
}
