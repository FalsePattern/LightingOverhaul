package lightingoverhaul.asm;

import java.io.IOException;

import lightingoverhaul.ModInfo;
import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class CoreAccessTransformer extends AccessTransformer {

    public CoreAccessTransformer() throws IOException {
        super("META-INF/" + ModInfo.MODID + "_at.cfg");
    }

}
