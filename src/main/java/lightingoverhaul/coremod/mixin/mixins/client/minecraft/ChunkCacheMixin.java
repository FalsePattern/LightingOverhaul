package lightingoverhaul.coremod.mixin.mixins.client.minecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.asm.mixin.Mixin;

import lightingoverhaul.coremod.helper.RGBHelper;

import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkCache.class)
@SideOnly(Side.CLIENT)
public abstract class ChunkCacheMixin {
    @Shadow public abstract int getSkyBlockTypeBrightness(EnumSkyBlock p_72810_1_, int p_72810_2_, int p_72810_3_, int p_72810_4_);

    @Inject(method = "getLightBrightnessForSkyBlocks",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    /*
     * Any Light rendered on a 1.8 Block goes through here
     */
    public void getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue, CallbackInfoReturnable<Integer> cir) {
        int skyBrightness = getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
        cir.setReturnValue(RGBHelper.computeLightBrightnessForSkyBlocks(skyBrightness, blockBrightness, lightValue));
    }
}
