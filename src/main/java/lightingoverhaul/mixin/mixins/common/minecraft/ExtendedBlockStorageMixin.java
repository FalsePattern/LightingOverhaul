package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.mixin.interfaces.IExtendedBlockStorageMixin;
import lombok.val;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

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
        rColorArray = array;
    }

    public void setGreenColorArray(NibbleArray array) {
        gColorArray = array;
    }

    public void setBlueColorArray(NibbleArray array) {
        bColorArray = array;
    }

    public void setRedColorArraySun(NibbleArray array) {
        rColorArraySun = array;
    }

    public void setGreenColorArraySun(NibbleArray array) {
        gColorArraySun = array;
    }

    public void setBlueColorArraySun(NibbleArray array) {
        bColorArraySun = array;
    }

    public NibbleArray getRedColorArray() {
        return rColorArray;
    }

    public NibbleArray getGreenColorArray() {
        return gColorArray;
    }

    public NibbleArray getBlueColorArray() {
        return bColorArray;
    }

    public NibbleArray getRedColorArraySun() {
        return rColorArraySun;
    }

    public NibbleArray getGreenColorArraySun() {
        return gColorArraySun;
    }

    public NibbleArray getBlueColorArraySun() {
        return bColorArraySun;
    }

    @Inject(at = @At("RETURN"),
            method = { "<init>" })
    public void init(CallbackInfo callbackInfo) {
        rColorArray = new NibbleArray(blockLSBArray.length, 4);
        gColorArray = new NibbleArray(blockLSBArray.length, 4);
        bColorArray = new NibbleArray(blockLSBArray.length, 4);
        rColorArraySun = new NibbleArray(blockLSBArray.length, 4);
        gColorArraySun = new NibbleArray(blockLSBArray.length, 4);
        bColorArraySun = new NibbleArray(blockLSBArray.length, 4);
    }

    @Redirect(method = "setExtBlocklightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;set(IIII)V"),
              require = 1)
    private void setExtBlockLightValueRGB(NibbleArray instance, int x, int y, int z, int value) {
        rColorArray.set(x, y, z, LightingApi.extractR(value));
        gColorArray.set(x, y, z, LightingApi.extractG(value));
        bColorArray.set(x, y, z, LightingApi.extractB(value));
    }

    @Redirect(method = "getExtBlocklightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;get(III)I"),
              require = 1)
    public int getExtBlockLightValueRGB(NibbleArray instance, int x, int y, int z) {
        return LightingApi.toLightBlock(
                rColorArray.get(x, y, z),
                gColorArray.get(x, y, z),
                bColorArray.get(x, y, z));
    }

    @Redirect(method = "setExtSkylightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;set(IIII)V"),
              require = 1)
    public void setExtSkylightValue(NibbleArray instance, int x, int y, int z, int value) {
        rColorArraySun.set(x, y, z, LightingApi.extractSunR(value));
        gColorArraySun.set(x, y, z, LightingApi.extractSunG(value));
        bColorArraySun.set(x, y, z, LightingApi.extractSunB(value));
    }

    @Redirect(method = "getExtSkylightValue",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/chunk/NibbleArray;get(III)I"),
              require = 1)
    public int getExtSkylightValue(NibbleArray instance, int x, int y, int z) {
        return LightingApi.toLightSun(
                rColorArraySun.get(x, y, z),
                gColorArraySun.get(x, y, z),
                bColorArraySun.get(x, y, z));
    }
}
