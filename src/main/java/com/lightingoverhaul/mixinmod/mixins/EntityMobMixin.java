package com.lightingoverhaul.mixinmod.mixins;

import com.lightingoverhaul.coremod.api.LightingApi;
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
    private int sunlightValueCheckFix(World worldObj, EnumSkyBlock p_72972_1_, int p_72972_2_, int p_72972_3_, int p_72972_4_) {
        int val = worldObj.getSavedLightValue(p_72972_1_, p_72972_2_, p_72972_3_, p_72972_4_);
        return LightingApi.getMaxChannelSun(val);
    }
}
