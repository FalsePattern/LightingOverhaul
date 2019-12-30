package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    /***
     * @author darkshadow44
     * @reason Prevent glitch on held items
     */
    @Inject(method = "renderItem(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V", at = @At("HEAD"), require = 1, remap = false)
    public void renderItem_start(EntityLivingBase entity, ItemStack p_78443_2_, int p_78443_3_, ItemRenderType type, CallbackInfo callback) {
        Minecraft.getMinecraft().entityRenderer.disableLightmap(0);
    }

    /***
     * @author darkshadow44
     * @reason Prevent glitch on held items
     */
    @Inject(method = "renderItem (Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;ILnet/minecraftforge/client/IItemRenderer$ItemRenderType;)V", at = @At("RETURN"), require = 1, remap = false)
    public void renderItem_end(EntityLivingBase entity, ItemStack p_78443_2_, int p_78443_3_, ItemRenderType type, CallbackInfo callback) {
        Minecraft.getMinecraft().entityRenderer.enableLightmap(0);
    }
}
