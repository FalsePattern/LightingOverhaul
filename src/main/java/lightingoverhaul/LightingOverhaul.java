package lightingoverhaul;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import lightingoverhaul.fmlevents.ChunkDataEventHandler;
import lightingoverhaul.network.PacketHandler;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.mixin.interfaces.IBlockMixin;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import lombok.val;
import net.minecraft.block.Block;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("UnstableApiUsage")
public class LightingOverhaul extends DummyModContainer {
    public static Logger LOlog = LogManager.getLogger(ModInfo.MODNAME);
    public ChunkDataEventHandler chunkDataEventHandler;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights
    public static Object dynamicLights;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights.getLightValue
    public static Method getDynamicLight = null;

    public LightingOverhaul() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = ModInfo.MODID;
        meta.name = ModInfo.MODNAME;
        meta.version = ModInfo.VERSION;
        meta.url = ModInfo.URL;
        meta.credits = ModInfo.CREDITS;
        meta.authorList = Arrays.asList(ModInfo.AUTHORS);
        meta.description = ModInfo.DESCRIPTION;
        meta.useDependencyInformation = true;
        chunkDataEventHandler = new ChunkDataEventHandler();
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);

        return true;
    }

    static void setSkyColor() {
        try {
            Field field = EnumSkyBlock.class.getDeclaredFields()[2];
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            int color = (15 << LightingApi._bitshift_sun_r) | (15 << LightingApi._bitshift_sun_g) | (15 << LightingApi._bitshift_sun_b) | 15;
            field.set(EnumSkyBlock.Sky, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent evt) {

        Config.loadConfig(new File(evt.getModConfigurationDirectory(), ModInfo.MODID + ".cfg"));

        LOlog.info("Starting up " + ModInfo.MODNAME);

        // Spin up network handler
        PacketHandler.init();

        // Hook into chunk events
        MinecraftForge.EVENT_BUS.register(chunkDataEventHandler);

        setSkyColor();
    }

    private static void setLightValue(Block block, int metadata, int r, int g, int b) {
        IBlockMixin ib = (IBlockMixin) block;
        int light = LightingApi.makeRGBLightValue(r, g, b);
        if (metadata == 0)
            ib.setLightValue(light);
        else
            ib.setMetadataLightValue(metadata, light);
    }

    public static boolean postInitRun = false;
    @Subscribe
    public void postInit(FMLPostInitializationEvent evt) {

        LOlog.info("Beginning custom light value injection...");
        int[] valueBuffer = new int[3];
        int failCount = 0;
        int l;
        for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
            if (block == null) continue;

            l = ((IBlockMixin) block).getLightValue_INTERNAL();
            if ((l <= 0) || (l > 0xF)) continue;

            String name = GameData.getBlockRegistry().getNameForObject(block);
            String formatStr;
            int metadata = 0;
            int nextMetadata = 0;
            while (metadata != -1) {
                val result = Config.getLightValueForMetadata(name, metadata, l, valueBuffer);
                if (!result.isPresent() || (nextMetadata = result.getAsInt()) != -1) {
                    formatStr = "Found light value for %s/%d in config: (r: %d, g: %d, b: %d)";
                    if (!result.isPresent()) nextMetadata = -1;
                } else {
                    failCount++;
                    formatStr = "Could not find light value for %s/%d in config. Replacing with auto-computed value: (r: %d, g: %d, b: %d)";
                }
                LOlog.info(String.format(formatStr, name, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]));
                setLightValue(block, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]);
                metadata = nextMetadata;
            }
        }
        if (failCount == 0) {
            LOlog.info("No missed block lights detected! A winner is you!");
        } else {
            LOlog.warn(String.format("%d missed block lights were detected and converted!", failCount));
        }

        LOlog.info("Checking for DynamicLights...");
        Class<?> DynamicLightsClazz = null;
        try {
            DynamicLightsClazz = Class.forName("atomicstryker.dynamiclights.client.DynamicLights");
            Field instanceField = DynamicLightsClazz.getDeclaredField("instance");
            instanceField.setAccessible(true);
            dynamicLights = instanceField.get(null);
        } catch (ClassNotFoundException e) {
            LOlog.info("Dynamic Lights not found");
        } catch (NoSuchFieldException e) {
            LOlog.error("Missing field named \"instance\" in DynamicLights. Did versions change?");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOlog.error("We were denied access to the field \"instance\" in DynamicLights :(  Did versions change?");
            e.printStackTrace();
        }

        if (DynamicLightsClazz != null && dynamicLights != null) {
            LOlog.info("Hey DynamicLights... What's the plan?");

            try {
                getDynamicLight = DynamicLightsClazz.getDeclaredMethod("getLightValue", IBlockAccess.class, Block.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (NoSuchMethodException e) {
                LOlog.error("Missing method named \"getLightValue\" in DynamicLightsClazz. Did versions change?");
                e.printStackTrace();
            }
        }
        postInitRun = true;
        Config.syncConfig();
    }
}