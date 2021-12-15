package com.lightingoverhaul.mixinmod.mixins;

import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.mixinmod.interfaces.IExtendedBlockStorageMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin implements IExtendedBlockStorageMixin {

    @Shadow
    public byte[] blockLSBArray;

    @Shadow
    public NibbleArray blocklightArray;

    @Shadow
    private NibbleArray skylightArray;

    public NibbleArray rColorArray;
    public NibbleArray gColorArray;
    public NibbleArray bColorArray;
    public NibbleArray rColorArray2;
    public NibbleArray gColorArray2;
    public NibbleArray bColorArray2;
    public NibbleArray rColorArraySun;
    public NibbleArray gColorArraySun;
    public NibbleArray bColorArraySun;

    public void setRedColorArray(NibbleArray array) {
        this.rColorArray = array;
    }

    public void setGreenColorArray(NibbleArray array) {
        this.gColorArray = array;
    }

    public void setBlueColorArray(NibbleArray array) {
        this.bColorArray = array;
    }

    public void setRedColorArray2(NibbleArray array) {
        this.rColorArray2 = array;
    }

    public void setGreenColorArray2(NibbleArray array) {
        this.gColorArray2 = array;
    }

    public void setBlueColorArray2(NibbleArray array) {
        this.bColorArray2 = array;
    }

    public void setRedColorArraySun(NibbleArray array) {
        this.rColorArraySun = array;
    }

    public void setGreenColorArraySun(NibbleArray array) {
        this.gColorArraySun = array;
    }

    public void setBlueColorArraySun(NibbleArray array) {
        this.bColorArraySun = array;
    }

    public NibbleArray getRedColorArray() {
        return this.rColorArray;
    }

    public NibbleArray getGreenColorArray() {
        return this.gColorArray;
    }

    public NibbleArray getBlueColorArray() {
        return bColorArray;
    }

    public NibbleArray getRedColorArray2() {
        return this.rColorArray2;
    }

    public NibbleArray getGreenColorArray2() {
        return this.gColorArray2;
    }

    public NibbleArray getBlueColorArray2() {
        return bColorArray2;
    }

    public NibbleArray getRedColorArraySun() {
        return this.rColorArraySun;
    }

    public NibbleArray getGreenColorArraySun() {
        return this.gColorArraySun;
    }

    public NibbleArray getBlueColorArraySun() {
        return bColorArraySun;
    }

    @Inject(at = @At("RETURN"),
            method = { "<init>" })
    public void init(CallbackInfo callbackInfo) {
        this.rColorArray = new NibbleArray(this.blockLSBArray.length, 4);
        this.gColorArray = new NibbleArray(this.blockLSBArray.length, 4);
        this.bColorArray = new NibbleArray(this.blockLSBArray.length, 4);
        this.rColorArray2 = new NibbleArray(this.blockLSBArray.length, 4);
        this.gColorArray2 = new NibbleArray(this.blockLSBArray.length, 4);
        this.bColorArray2 = new NibbleArray(this.blockLSBArray.length, 4);
        this.rColorArraySun = new NibbleArray(this.blockLSBArray.length, 4);
        this.gColorArraySun = new NibbleArray(this.blockLSBArray.length, 4);
        this.bColorArraySun = new NibbleArray(this.blockLSBArray.length, 4);
    }

    @Redirect(method = "setExtBlocklightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;set(IIII)V"),
              require = 1)
    private void setExtBlockLightValueRGB(NibbleArray instance, int x, int y, int z, int value) {
        int r = (value >> LightingApi._bitshift_r) & LightingApi._bitmask;
        int g = (value >> LightingApi._bitshift_g) & LightingApi._bitmask;
        int b = (value >> LightingApi._bitshift_b) & LightingApi._bitmask;
        int normal = Math.max(Math.max(r, g), b);
        normal = Math.min(15, normal);

        instance.set(x, y, z, normal);
        this.rColorArray.set(x, y, z, r & 0xF);
        this.gColorArray.set(x, y, z, g & 0xF);
        this.bColorArray.set(x, y, z, b & 0xF);

        this.rColorArray2.set(x, y, z, r >> 4);
        this.gColorArray2.set(x, y, z, g >> 4);
        this.bColorArray2.set(x, y, z, b >> 4);
    }

    @Redirect(method = "getExtBlocklightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;get(III)I"),
              require = 1)
    public int getExtBlockLightValueRGB(NibbleArray instance, int x, int y, int z) {
        int normal = instance.get(x, y, z);
        int r = this.rColorArray.get(x, y, z);
        int g = this.gColorArray.get(x, y, z);
        int b = this.bColorArray.get(x, y, z);
        r |= this.rColorArray2.get(x, y, z) << 4;
        g |= this.gColorArray2.get(x, y, z) << 4;
        b |= this.bColorArray2.get(x, y, z) << 4;

        if (r == 0 && g == 0 && b == 0) {
            r = g = b = normal;
        } else if (r != g && g != b && b != r){
            normal = normal; //noop for breakpoint
        }
        normal = Math.max(Math.max(r, g), b);
        normal = Math.min(15, normal);

        int ret = normal;
        ret |= r << LightingApi._bitshift_r;
        ret |= g << LightingApi._bitshift_g;
        ret |= b << LightingApi._bitshift_b;

        return ret;
    }

    @Redirect(method = "setExtSkylightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;set(IIII)V"),
              require = 1)
    public void setExtSkylightValue(NibbleArray instance, int x, int y, int z, int value) {
        int r = (value >> LightingApi._bitshift_sun_r) & LightingApi._bitmask_sun;
        int g = (value >> LightingApi._bitshift_sun_g) & LightingApi._bitmask_sun;
        int b = (value >> LightingApi._bitshift_sun_b) & LightingApi._bitmask_sun;
        int normal = Math.max(Math.max(r, g), b);

        instance.set(x, y, z, normal);
        this.rColorArraySun.set(x, y, z, r);
        this.gColorArraySun.set(x, y, z, g);
        this.bColorArraySun.set(x, y, z, b);
    }

    @Redirect(method = "getExtSkylightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;get(III)I"),
              require = 1)
    public int getExtSkylightValue(NibbleArray instance, int x, int y, int z) {
        int normal = instance.get(x, y, z);
        int r = this.rColorArraySun.get(x, y, z);
        int g = this.gColorArraySun.get(x, y, z);
        int b = this.bColorArraySun.get(x, y, z);

        if (r == 0 && g == 0 && b == 0) {
            r = g = b = normal;
        }
        normal = Math.max(Math.max(r, g), b);

        int ret = normal;
        ret |= r << LightingApi._bitshift_sun_r;
        ret |= g << LightingApi._bitshift_sun_g;
        ret |= b << LightingApi._bitshift_sun_b;

        return ret;
    }
}
