package coloredlightscore.src.asm;

import java.util.Map;

import org.apache.logging.log4j.LogManager;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.Name;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

@MCVersion("1.7.10")
@Name("LightOverhaulCore")
@SortingIndex(1001)
public class ColoredLightsCoreLoadingPlugin implements IFMLLoadingPlugin {
    public static org.apache.logging.log4j.Logger CLLog = LogManager.getLogger("lightoverhaulcore");

    @Override
    public String getModContainerClass() {
        return ColoredLightsCoreDummyContainer.class.getName();
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
        return ColoredLightsCoreAccessTransformer.class.getName();
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }
}
