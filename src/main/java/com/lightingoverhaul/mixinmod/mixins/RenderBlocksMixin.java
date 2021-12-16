package com.lightingoverhaul.mixinmod.mixins;

import com.lightingoverhaul.coremod.api.LightingApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.lightingoverhaul.mixinmod.helper.BlockHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public abstract class RenderBlocksMixin {

    @Redirect(method = { "renderBlockLiquid" },
              at = @At(value = "INVOKE",
                       target = "net.minecraft.block.Block.getMixedBrightnessForBlock(Lnet/minecraft/world/IBlockAccess;III)I"),
              require = 3)
    public int renderBlockLiquid_inject(Block block, IBlockAccess blockAccess, int x, int y, int z) {
        return BlockHelper.getMixedBrightnessForBlockWithColor(blockAccess, x, y + 1, z);
    }

    @Redirect(method = "renderStandardBlockWithColorMultiplier",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/block/Block;getMixedBrightnessForBlock(Lnet/minecraft/world/IBlockAccess;III)I"),
              require = 7)
    private int rsbwcm_colorFix(Block ignored1, IBlockAccess blockAccess, int x, int y, int z) {
        return BlockHelper.getMixedBrightnessForBlockWithColor(blockAccess, x, y, z);
    }

    @Inject(method = "getAoBrightness",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1
    )
    public void getAoBrightness(int a, int b, int c, int l, CallbackInfoReturnable<Integer> cir) {

        // Must mix all 5 channels now
        cir.setReturnValue(mixColorChannel(LightingApi._bitshift_sun_r2, a, b, c, l) | // SSSS
                mixColorChannel(LightingApi._bitshift_sun_g2, a, b, c, l) | // SSSS
                mixColorChannel(LightingApi._bitshift_sun_b2, a, b, c, l) | // SSSS
                mixColorChannel(LightingApi._bitshift_b2, a, b, c, l) | // BBBB
                mixColorChannel(LightingApi._bitshift_g2, a, b, c, l) | // GGGG this is the problem child
                mixColorChannel(LightingApi._bitshift_r2, a, b, c, l) | // RRRR
                mixColorChannel(LightingApi._bitshift_l2, a, b, c, l) // LLLL
        );
    }

    public int mixColorChannel(int startBit, int p1, int p2, int p3, int p4) {
        int avg;

        int q1 = (p1 >>> startBit) & 0xf;
        int q2 = (p2 >>> startBit) & 0xf;
        int q3 = (p3 >>> startBit) & 0xf;
        int q4 = (p4 >>> startBit) & 0xf;

        if (q1 == 0) q1 = q4;
        if (q2 == 0) q2 = q4;
        if (q3 == 0) q3 = q4;

        avg = (q1 + q2 + q3 + q4) / 4;

        return avg << startBit;
    }
}
