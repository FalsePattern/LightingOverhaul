package com.lightingoverhaul.coremod.asm;

import java.io.IOException;

import com.lightingoverhaul.Tags;
import cpw.mods.fml.common.asm.transformers.AccessTransformer;

public class CoreAccessTransformer extends AccessTransformer {

    public CoreAccessTransformer() throws IOException {
        super("META-INF/" + Tags.MODID + "_at.cfg");
    }

}
