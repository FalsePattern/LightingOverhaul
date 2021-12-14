package com.lightingoverhaul.mixinmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lightingoverhaul.coremod.api.LightingApi;
import net.minecraft.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Shadow
    public int lightValue;

    @Inject(method = "setLightLevel",
            at = @At(value="RETURN"),
            require = 1
    )
    public void setLightLevel(float par1, CallbackInfoReturnable<?> callback) {
        // Clamp negative values
        if (par1 < 0.0F) {
            par1 = 0.0F;
        }

        if (par1 <= 1.0F) {
            // If the incoming light value is a plain white call, then "color" the light
            // value white
            this.lightValue = LightingApi.makeRGBLightValue(par1, par1, par1);
        } else {
            // Otherwise, let whatever it is through
            this.lightValue = (int) (15.0F * par1);
        }
    }


}
