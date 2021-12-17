package com.lightingoverhaul.mixinmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.lightingoverhaul.coremod.api.LightingApi;
import net.minecraft.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin {
    private float par1;
    @ModifyVariable(method = "setLightLevel",
                    at = @At(value = "HEAD"),
                    ordinal = 0,
                    require = 1,
                    argsOnly = true)
    private float getLL(float value) {
        return par1 = value;
    }

    @Redirect(method = "setLightLevel",
              at = @At(value="FIELD",
                       target = "Lnet/minecraft/block/Block;lightValue:I"),
              require = 1
    )
    public void setLightLevel(Block instance, int value) {
        // Clamp negative values
        if (par1 < 0.0F) {
            par1 = 0.0F;
        }

        if (par1 <= 1.0F) {
            // If the incoming light value is a plain white call, then "color" the light
            // value white
            instance.lightValue = LightingApi.makeRGBLightValue(par1, par1, par1);
        } else {
            // Otherwise, let whatever it is through
            instance.lightValue = value;
        }
    }


}
