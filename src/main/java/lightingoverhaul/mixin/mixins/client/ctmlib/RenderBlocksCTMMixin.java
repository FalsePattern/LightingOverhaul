package lightingoverhaul.mixin.mixins.client.ctmlib;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lightingoverhaul.mixin.util.CTMHax;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(team.chisel.ctmlib.RenderBlocksCTM.class)
@SideOnly(Side.CLIENT)
public abstract class RenderBlocksCTMMixin {

    @Inject(method = "avg",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void avg(int[] lightVals, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(CTMHax.avg(lightVals));
    }
}
