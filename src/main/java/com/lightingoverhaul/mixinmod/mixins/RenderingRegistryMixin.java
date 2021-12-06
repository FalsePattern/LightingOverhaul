package com.lightingoverhaul.mixinmod.mixins;

import java.util.Map;

import com.lightingoverhaul.mixinmod.interfaces.ITessellatorMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

@Mixin(RenderingRegistry.class)
public abstract class RenderingRegistryMixin {

    @Shadow(remap = false)
    private Map<Integer, ISimpleBlockRenderingHandler> blockRenderers;

    public boolean renderWorldBlock(RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelId) {
        ITessellatorMixin tessellatorMixin = (ITessellatorMixin) Tessellator.instance;
        if (!this.blockRenderers.containsKey(modelId)) {
            return false;
        }
        ISimpleBlockRenderingHandler bri = this.blockRenderers.get(modelId);

        String className = block.getClass().getName();
        boolean doLock = className.startsWith("com.carpentersblocks.");

        if (doLock) {
            int light = block.getMixedBrightnessForBlock(world, x, y, z);
            Tessellator.instance.setBrightness(light);
            tessellatorMixin.setLockedBrightness(true);
        }
        boolean ret = bri.renderWorldBlock(world, x, y, z, block, modelId, renderer);
        if (doLock) {
            tessellatorMixin.setLockedBrightness(false);
        }

        return ret;
    }
}
