package com.lightingoverhaul.mixinmod.mixinplugin;

import com.lightingoverhaul.Tags;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import lombok.val;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.*;

import static com.lightingoverhaul.mixinmod.mixinplugin.CompatibilityTier.*;
import static com.lightingoverhaul.mixinmod.mixinplugin.TargetedMod.*;

public enum Mixin {

    //Both sides
    BlockMixin(builder().unit(Regular, "BlockMixin")),
    ChunkCacheMixin(builder().unit(InjectCancel, "ChunkCacheMixin")),
    ChunkMixin(builder().unit(InjectCancel, "ChunkMixin")),
    EntityMobMixin(builder().unit(Regular, "EntityMobMixin")),
    EntityPlayerMPMixin(builder().unit(Regular, "EntityPlayerMPMixin")),
    ExtendedBlockStorageMixin(builder().unit(Regular, "ExtendedBlockStorageMixin")),
    WorldMixin(builder().unit(InjectCancel, "WorldMixin")),

    //Client only
    EntityRendererMixin(builder().side(Side.CLIENT).unit(InjectCancel, "EntityRendererMixin")),
    OpenGLHelperMixin(builder().side(Side.CLIENT).unit(InjectCancel, "OpenGLHelperMixin")),
    RenderBlocksMixin(builder().side(Side.CLIENT).unit(InjectCancel, "RenderBlocksMixin")),
    RenderingRegistryMixin(builder().side(Side.CLIENT).unit(Regular, "RenderingRegistryMixin")),
    TessellatorMixin(builder().side(Side.CLIENT).unit(InjectCancel, "TessellatorMixin"));

    public final MixinUnit[] units;
    public final Set<TargetedMod> targetedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.units = builder.units.toArray(new MixinUnit[0]);
        this.targetedMods = builder.targetedMods;
        this.side = builder.side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.BOTH
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }

    public String getBestAlternativeForTier(CompatibilityTier tier) {
        for (val unit: units) {
            if (unit.tier.isTierBetterThan(tier)) return unit.mixinClass;
        }
        throw new MixinException("Failed to retrieve mixin alternative for " + this.name() + " in mod " + Tags.MODID);
    }

    private static Builder builder() {
        return builder(true);
    }

    private static Builder builder(boolean withVanilla) {
        return withVanilla ? new Builder().target(VANILLA) : new Builder();
    }

    private static class Builder {
        public ArrayList<MixinUnit> units = new ArrayList<>();
        public Side side = Side.BOTH;
        public Set<TargetedMod> targetedMods = new HashSet<>();

        public Builder unit(CompatibilityTier tier, String mixinClass) {
            units.add(new MixinUnit(tier, mixinClass));
            return this;
        }

        public Builder target(TargetedMod mod) {
            targetedMods.add(mod);
            return this;
        }

        public Builder side(Side side) {
            this.side = side;
            return this;
        }
    }

    private static class MixinUnit {
        public final CompatibilityTier tier;
        public final String mixinClass;

        public MixinUnit(CompatibilityTier tier, String mixinClass) {
            this.tier = tier;
            this.mixinClass = mixinClass;
        }
    }

    private enum Side {
        BOTH,
        CLIENT,
        SERVER
    }
}

