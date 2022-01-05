package lightingoverhaul.coremod.asm;

import java.util.Map;

import lightingoverhaul.Tags;
import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@MCVersion("1.7.10")
@Name(Tags.MODID + "core")
@SortingIndex(1001)
public class CoreLoadingPlugin implements IFMLLoadingPlugin {
    public static org.apache.logging.log4j.Logger CLLog = LogManager.getLogger(Tags.MODID + "core");

    @Override
    public String getModContainerClass() {
        return LightingOverhaulCore.class.getName();
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
        return CoreAccessTransformer.class.getName();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { TextureTransformer.class.getName() };
    }
}
