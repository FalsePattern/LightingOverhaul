package com.lightingoverhaul.mixinmod.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@Mixin(EntityMob.class)
public abstract class EntityMobMixin extends EntityCreature {

    public EntityMobMixin(World p_i1602_1_) {
        super(p_i1602_1_);
    }

    /***
     * @author darkshadow44
     * @reason TODO
     */
    @Overwrite
    protected boolean isValidLightLevel() {
        int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.boundingBox.minY);
        int k = MathHelper.floor_double(this.posZ);
        // REMOVED: if (this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) >
        // this.rand.nextInt(32))
        // REMOVED: if return false;
        int m = this.worldObj.getBlockLightValue(i, j, k);
        if (this.worldObj.isThundering()) {
            int n = this.worldObj.skylightSubtracted;
            this.worldObj.skylightSubtracted = 10;
            m = this.worldObj.getBlockLightValue(i, j, k);
            this.worldObj.skylightSubtracted = n;
        }
        return (m <= this.rand.nextInt(8));
    }
}
