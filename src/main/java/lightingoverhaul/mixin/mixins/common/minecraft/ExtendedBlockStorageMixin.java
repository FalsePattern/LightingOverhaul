package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.mixin.interfaces.IExtendedBlockStorageMixin;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendedBlockStorage.class)
public abstract class ExtendedBlockStorageMixin implements IExtendedBlockStorageMixin {

    public NibbleArray rColorArray;
    public NibbleArray gColorArray;
    public NibbleArray bColorArray;
    public NibbleArray rColorArraySun;
    public NibbleArray gColorArraySun;
    public NibbleArray bColorArraySun;

    @Shadow
    private byte[] blockLSBArray;

    public void setRedColorArray(NibbleArray array) {
        this.rColorArray = array;
    }

    public void setGreenColorArray(NibbleArray array) {
        this.gColorArray = array;
    }

    public void setBlueColorArray(NibbleArray array) {
        this.bColorArray = array;
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

        instance.set(x, y, z, normal);
        this.rColorArray.set(x, y, z, r & LightingApi._bitmask);
        this.gColorArray.set(x, y, z, g & LightingApi._bitmask);
        this.bColorArray.set(x, y, z, b & LightingApi._bitmask);
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

        if (r == 0 && g == 0 && b == 0) {
            r = g = b = normal;
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
        int r = (value >> LightingApi._bitshift_sun_r) & LightingApi._bitmask;
        int g = (value >> LightingApi._bitshift_sun_g) & LightingApi._bitmask;
        int b = (value >> LightingApi._bitshift_sun_b) & LightingApi._bitmask;
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
