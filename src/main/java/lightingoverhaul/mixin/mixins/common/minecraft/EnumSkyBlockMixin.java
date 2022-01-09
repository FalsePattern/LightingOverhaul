package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.api.LightingApi;
import net.minecraft.world.EnumSkyBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static lightingoverhaul.LightingOverhaul.LOlog;

@Mixin(EnumSkyBlock.class)
public abstract class EnumSkyBlockMixin {
    @ModifyConstant(method = "<clinit>",
                    constant = @Constant(intValue = 15),
                    require = 1)
    private static int hackValue(int value) {
        switch (value) {
            case 15:
                return LightingApi.toLightSun(15, 15, 15);
            case 0:
                return 0;
            default:
                LOlog.fatal("Somebody else tampered with EnumSkyBlock! Things are not going to play together nicely!");
                return value;
        }
    }
}
