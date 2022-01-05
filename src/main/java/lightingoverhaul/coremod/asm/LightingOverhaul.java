package lightingoverhaul.coremod.asm;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import lightingoverhaul.Tags;
import lightingoverhaul.Config;
import lightingoverhaul.coremod.fmlevents.ChunkDataEventHandler;
import lightingoverhaul.coremod.network.PacketHandler;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lightingoverhaul.api.LightingApi;
import lightingoverhaul.coremod.mixin.interfaces.IBlockMixin;
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

@SuppressWarnings("UnstableApiUsage")
public class LightingOverhaul extends DummyModContainer {
    public ChunkDataEventHandler chunkDataEventHandler;

    public static boolean emissivesEnabled = false;

    // This is picked up and replaced by the build.gradle
    public static final String version = Tags.VERSION;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights
    public static Object dynamicLights;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights.getLightValue
    public static Method getDynamicLight = null;

    public LightingOverhaul() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = Tags.MODID;
        meta.name = Tags.MODNAME;
        meta.version = version;
        meta.logoFile = "/mod_" + Tags.MODID + ".logo.png";
        meta.url = "https://github.com/FalsePattern/LightingOverhaul";
        meta.credits = "";
        meta.authorList = Arrays.asList("heaton84", "Murray65536", "Kovu", "Biggerfisch", "CptSpaceToaster", "DarkShadow44", "FalsePattern");
        meta.description = "Bringing colored lights to 1.7.10!";
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

        Config.loadConfig(new File(evt.getModConfigurationDirectory(), Tags.MODID + ".cfg"));

        CoreLoadingPlugin.CLLog = evt.getModLog();

        CoreLoadingPlugin.CLLog.info("Starting up ColoredLightsCore");

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

        CoreLoadingPlugin.CLLog.info("Beginning custom light value injection...");
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
                CoreLoadingPlugin.CLLog.info(String.format(formatStr, name, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]));
                setLightValue(block, metadata, valueBuffer[0], valueBuffer[1], valueBuffer[2]);
                metadata = nextMetadata;
            }
        }
        if (failCount == 0) {
            CoreLoadingPlugin.CLLog.info("No missed block lights detected! A winner is you!");
        } else {
            CoreLoadingPlugin.CLLog.warn(String.format("%d missed block lights were detected and converted!", failCount));
        }

        CoreLoadingPlugin.CLLog.info("Checking for DynamicLights...");
        Class<?> DynamicLightsClazz = null;
        try {
            DynamicLightsClazz = Class.forName("atomicstryker.dynamiclights.client.DynamicLights");
            Field instanceField = DynamicLightsClazz.getDeclaredField("instance");
            instanceField.setAccessible(true);
            dynamicLights = instanceField.get(null);
        } catch (ClassNotFoundException e) {
            CoreLoadingPlugin.CLLog.info("Dynamic Lights not found");
        } catch (NoSuchFieldException e) {
            CoreLoadingPlugin.CLLog.error("Missing field named \"instance\" in DynamicLights. Did versions change?");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            CoreLoadingPlugin.CLLog.error("We were denied access to the field \"instance\" in DynamicLights :(  Did versions change?");
            e.printStackTrace();
        }

        if (DynamicLightsClazz != null && dynamicLights != null) {
            CoreLoadingPlugin.CLLog.info("Hey DynamicLights... What's the plan?");

            try {
                getDynamicLight = DynamicLightsClazz.getDeclaredMethod("getLightValue", IBlockAccess.class, Block.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            } catch (NoSuchMethodException e) {
                CoreLoadingPlugin.CLLog.error("Missing method named \"getLightValue\" in DynamicLightsClazz. Did versions change?");
                e.printStackTrace();
            }
        }
        postInitRun = true;
        Config.syncConfig();
    }
}