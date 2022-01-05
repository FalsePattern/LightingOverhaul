package lightingoverhaul.forgemod;

import lightingoverhaul.Tags;
import lightingoverhaul.forgemod.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID, name = Tags.MODNAME, version = Tags.VERSION, dependencies = "required-after:" + Tags.MODID + "core")
public class ModFile {
    @SidedProxy(clientSide = Tags.GROUPNAME + ".forgemod.proxy.ClientProxy", serverSide = Tags.GROUPNAME + ".forgemod.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}