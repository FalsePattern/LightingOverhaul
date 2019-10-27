 package cpw.mods.fml.client.registry;

import java.util.Map;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class RenderingRegistry {
    public Map<Integer, ISimpleBlockRenderingHandler> blockRenderers;
}
