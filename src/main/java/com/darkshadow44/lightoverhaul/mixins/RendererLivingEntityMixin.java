package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkshadow44.lightoverhaul.interfaces.ITessellatorMixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
public class RendererLivingEntityMixin {

    /***
     * @author darkshadow44
     * @reason Handle overlay textures of entities, for example on hit
     */
    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.OpenGlHelper.setActiveTexture(I)V", ordinal = 1))
    public void doRender_start(EntityLivingBase instance, double d1, double d2, double d3, float f1, float f2, CallbackInfo callback) {
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
    }

    /***
     * @author darkshadow44
     * @reason Handle overlay textures of entities, for example on hit
     */
    @Inject(method = "doRender", at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.OpenGlHelper.setActiveTexture(I)V", ordinal = 3))
    public void doRender_end(EntityLivingBase instance, double d1, double d2, double d3, float f1, float f2, CallbackInfo callback) {
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }
}