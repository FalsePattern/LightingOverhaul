package com.lightingoverhaul.coremod.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.lightingoverhaul.Tags;
import com.lightingoverhaul.coremod.fmlevents.ChunkDataEventHandler;
import com.lightingoverhaul.coremod.network.PacketHandler;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.lightingoverhaul.coremod.api.LightingApi;
import com.lightingoverhaul.mixinmod.interfaces.IBlockMixin;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;

@SuppressWarnings("UnstableApiUsage")
public class CoreDummyContainer extends DummyModContainer {
    public ChunkDataEventHandler chunkDataEventHandler;

    // This is picked up and replaced by the build.gradle
    public static final String version = Tags.VERSION;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights
    public static Object dynamicLights;

    // Reference to atomicstryker.dynamiclights.client.DynamicLights.getLightValue
    public static Method getDynamicLight = null;

    public CoreDummyContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = Tags.MODID + "core";
        meta.name = Tags.MODNAME + " Core";
        meta.version = version;
        meta.logoFile = "/mod_" + Tags.MODID + "core.logo.png";
        meta.url = "https://github.com/FalsePattern/LightingOverhaul";
        meta.credits = "";
        meta.authorList = Arrays.asList("heaton84", "Murray65536", "Kovu", "Biggerfisch", "CptSpaceToaster", "DarkShadow44", "FalsePattern");
        meta.description = "The coremod for Lighting Overhaul";
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

        CoreLoadingPlugin.CLLog = evt.getModLog();

        CoreLoadingPlugin.CLLog.info("Starting up ColoredLightsCore");

        // Spin up network handler
        PacketHandler.init();

        // Hook into chunk events
        MinecraftForge.EVENT_BUS.register(chunkDataEventHandler);

        setSkyColor();
    }

    private static void setLightValue(Block block, int r, int g, int b) {
        ((IBlockMixin) block).setLightValue(LightingApi.makeRGBLightValue(r, g, b));
    }

    @Subscribe
    public void postInit(FMLPostInitializationEvent evt) {
        // Inject RGB values into vanilla blocks
        setLightValue(Blocks.lava, 15, 10, 0);
        setLightValue(Blocks.flowing_lava, 15, 10, 0);
        setLightValue(Blocks.torch, 14, 13, 10);
        setLightValue(Blocks.fire, 15, 13, 0);
        setLightValue(Blocks.lit_redstone_ore, 9, 0, 0);
        setLightValue(Blocks.redstone_torch, 7, 0, 0);
        setLightValue(Blocks.portal, 6, 3, 11);
        setLightValue(Blocks.lit_furnace, 13, 12, 10);
        setLightValue(Blocks.powered_repeater, 9, 0, 0);
        setLightValue(Blocks.glowstone, 15, 15, 12);

        Block thisShouldBeABlock;
        int l;
        for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {
            thisShouldBeABlock = block;
            if (thisShouldBeABlock != null) {
                l = ((IBlockMixin) thisShouldBeABlock).getLightValue();
                if ((l > 0) && (l <= 0xF)) {
                    CoreLoadingPlugin.CLLog.info(thisShouldBeABlock.getLocalizedName() + "has light:" + l + ", but no color");
                    ((IBlockMixin) thisShouldBeABlock).setLightValue((l << 15) | (l << 10) | (l << 5) | l); // copy vanilla brightness into each color component to make it white/grey.
                }
            }
        }

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
    }
}