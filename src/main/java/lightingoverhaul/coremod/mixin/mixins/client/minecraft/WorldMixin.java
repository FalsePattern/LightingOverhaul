package lightingoverhaul.coremod.mixin.mixins.client.minecraft;

import lightingoverhaul.coremod.helper.RGBHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
@SideOnly(Side.CLIENT)
public abstract class WorldMixin {
    @Shadow
    public abstract int getSkyBlockTypeBrightness(EnumSkyBlock paramEnumSkyBlock, int paramInt1, int paramInt2, int paramInt3);

    @Inject(method = "getLightBrightnessForSkyBlocks",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    public void getLightBrightnessForSkyBlocks(int x, int y, int z, int lightValue, CallbackInfoReturnable<Integer> cir) {
        int skyBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockBrightness = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);
        cir.setReturnValue(RGBHelper.computeLightBrightnessForSkyBlocks(skyBrightness, blockBrightness, lightValue));
    }

}
