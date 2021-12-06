package com.lightingoverhaul.forgemod;

import com.lightingoverhaul.Tags;
import com.lightingoverhaul.forgemod.proxy.CommonProxy;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION)
public class ModFile {
    @SidedProxy(clientSide = Tags.GROUPNAME + ".forgemod.proxy.ClientProxy", serverSide = Tags.GROUPNAME + ".forgemod.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static CLWorldGen wg = new CLWorldGen();

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        CLMaterialsController.init();
        CLMaterialsController.registerMaterials();
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        CLMaterialsController.addRecipes();
        GameRegistry.registerWorldGenerator(wg, 10000);
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {

    }
}