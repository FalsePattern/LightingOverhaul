package lightingoverhaul;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@MCVersion("1.7.10")
@Name(ModInfo.MODID)
@SortingIndex(1001)
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    public static org.apache.logging.log4j.Logger CLLog = org.apache.logging.log4j.LogManager.getLogger(ModInfo.MODNAME);

    @Override
    public String getModContainerClass() {
        return ModInfo.GROUPNAME + ".LightingOverhaul";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { ModInfo.GROUPNAME + ".asm.TextureTransformer" };
    }
}
