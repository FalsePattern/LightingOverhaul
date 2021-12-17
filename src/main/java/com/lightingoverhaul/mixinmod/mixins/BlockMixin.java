package com.lightingoverhaul.mixinmod.mixins;

import com.lightingoverhaul.mixinmod.interfaces.IBlockMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.lightingoverhaul.coremod.api.LightingApi;
import net.minecraft.block.Block;

@Mixin(Block.class)
public abstract class BlockMixin implements IBlockMixin {
    @Shadow protected int lightValue;
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
        if (par1 < 0.0F) {
            par1 = 0.0F;
        }

        if (par1 <= 1.0F) {
            // If the incoming light value is a plain white call, then "color" the light
            // value white
            this.lightValue = LightingApi.makeRGBLightValue(par1, par1, par1);
        } else {
            // Otherwise, let whatever it is through
            this.lightValue = value;
        }
    }

    @Override
    public void setLightValue(int newValue) {
        lightValue = newValue;
    }

    @Override
    public int getLightValue() {
        return lightValue;
    }
}
