package lightingoverhaul;

import java.util.Map;

import coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@MCVersion("1.7.10")
@Name(ModInfo.MODID + "core")
@SortingIndex(1001)
public class CoreLoadingPlugin implements IFMLLoadingPlugin {

    static {
        ColoredLightsCoreLoadingPlugin.touch(); // Trigger class loading for fastcraft compatibility
    }

    @Override
    public String getModContainerClass() {
        return null;
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
        return new String[] { ModInfo.GROUPNAME + ".asm.Transformer" };
    }
}
