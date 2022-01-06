package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.LightingOverhaul;
import lightingoverhaul.CoreLoadingPlugin;
import lightingoverhaul.mixin.interfaces.IBlockMixin;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import lightingoverhaul.api.LightingApi;
import lombok.val;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lightingoverhaul.LightingOverhaul.LOlog;

@Mixin(Block.class)
public abstract class BlockMixin implements IBlockMixin {
    @Shadow
    protected int lightValue;

    private TIntIntMap metaLight = null;
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

        if (LightingOverhaul.postInitRun && par1 <= 1.0F) {
            LOlog.warn("setLightLevel called after LightingOverhaul already transformed light values! Converting to grayscale.");
            // If the incoming light value is a plain white call, then "color" the light
            // value white
            setLightValue(LightingApi.makeRGBLightValue(par1, par1, par1));
        } else {
            // Otherwise, let whatever it is through
            setLightValue(value);
        }
    }

    @Inject(method = "getLightValue(Lnet/minecraft/world/IBlockAccess;III)I",
            at = @At(value = "HEAD"),
            cancellable = true,
            remap = false,
            require = 1)
    private void metaLightValueBypass(IBlockAccess world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
        if (metaLight == null) return;
        val meta = world.getBlockMetadata(x, y, z);
        if (metaLight.containsKey(meta)) {
            cir.setReturnValue(metaLight.get(meta));
        }
    }

    @Override
    public void setLightValue(int newValue) {
        lightValue = newValue;
    }

    @Override
    public int getLightValue_INTERNAL() {
        return lightValue;
    }

    @Override
    public void setMetadataLightValue(int metadata, int lightValue) {
        if (metaLight == null) metaLight = new TIntIntHashMap();
        metaLight.put(metadata, lightValue);
    }


}
