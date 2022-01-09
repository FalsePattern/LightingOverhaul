package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.api.LightingApi;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityMob.class)
public abstract class EntityMobMixin {

    @Redirect(method="isValidLightLevel",
              at= @At(value="INVOKE",
                      target="Lnet/minecraft/world/World;getSavedLightValue(Lnet/minecraft/world/EnumSkyBlock;III)I"),
              require = 1)
    private int sunlightValueCheckFix(World worldObj, EnumSkyBlock sky, int x, int y, int z) {
        int val = worldObj.getSavedLightValue(sky, x, y, z);
        return LightingApi.getHighestValueFromPackedSun(val);
    }
}
