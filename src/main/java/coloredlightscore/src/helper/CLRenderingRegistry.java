package coloredlightscore.src.helper;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

public class CLRenderingRegistry {

    public static boolean renderWorldBlock(RenderingRegistry instance, RenderBlocks renderer, IBlockAccess world, int x, int y, int z, Block block, int modelId)
    {
        if (!instance.blockRenderers.containsKey(modelId)) { return false; }
        ISimpleBlockRenderingHandler bri = instance.blockRenderers.get(modelId);

        String className = block.getClass().getName();
        boolean doLock = className.startsWith("com.carpentersblocks.");

        if (doLock)
        {
            int light = block.getMixedBrightnessForBlock(world, x, y, z);
            CLTessellatorHelper.setBrightness(Tessellator.instance, light);
            CLTessellatorHelper.lockedBrightness = true;
        }
        boolean ret = bri.renderWorldBlock(world, x, y, z, block, modelId, renderer);
        if (doLock)
        {
            CLTessellatorHelper.lockedBrightness = false;
        }

        return ret;
    }
}
