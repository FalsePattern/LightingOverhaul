package com.lightingoverhaul.mixinmod.mixinplugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Arrays;
import java.util.List;

public enum Mixin {

    ChunkMixin("ChunkMixin", Side.BOTH, TargetedMod.VANILLA),
    TessellatorMixin("TessellatorMixin", Side.CLIENT, TargetedMod.VANILLA),
    ChunkCacheMixin("ChunkCacheMixin", Side.BOTH, TargetedMod.VANILLA),
    EntityRendererMixin("EntityRendererMixin", Side.CLIENT, TargetedMod.VANILLA),
    RenderingRegistryMixin("RenderingRegistryMixin", Side.CLIENT, TargetedMod.VANILLA),
    OpenGlHelperMixin("OpenGlHelperMixin", Side.CLIENT, TargetedMod.VANILLA),
    RenderBlocksMixin("RenderBlocksMixin", Side.CLIENT, TargetedMod.VANILLA),
    ExtendedBlockStorageMixin("ExtendedBlockStorageMixin", Side.BOTH, TargetedMod.VANILLA),
    EntityPlayerMPMixin("EntityPlayerMPMixin", Side.BOTH, TargetedMod.VANILLA),
    EntityMobMixin("EntityMobMixin", Side.BOTH, TargetedMod.VANILLA),
    WorldMixin("WorldMixin", Side.BOTH, TargetedMod.VANILLA),
    BlockMixin("BlockMixin", Side.BOTH, TargetedMod.VANILLA);

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    private final Side side;

    Mixin(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = side;
    }

    Mixin(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = Side.BOTH;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.BOTH
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }
}

