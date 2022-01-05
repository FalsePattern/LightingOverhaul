package lightingoverhaul.forgemod.proxy;

import lightingoverhaul.coremod.Config;
import lightingoverhaul.forgemod.CLMaterialsController;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        if (Config.enableInGameContent()) {
            CLMaterialsController.init();
            CLMaterialsController.registerMaterials();
        }
    }

    public void init(FMLInitializationEvent event) {
        if (Config.enableInGameContent()) {
            CLMaterialsController.addRecipes();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}