package lightingoverhaul.mixin.plugin;

import lightingoverhaul.ModInfo;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import lombok.val;
import org.spongepowered.asm.mixin.throwables.MixinException;

import java.util.*;

import static lightingoverhaul.mixin.plugin.TargetedMod.*;

public enum Mixin {

    BlockMixin(builder(Side.COMMON).unit(CompatibilityTier.Regular, "minecraft.BlockMixin")),
    ChunkMixin(builder(Side.COMMON).unit(CompatibilityTier.InjectCancel, "minecraft.ChunkMixin")),
    EntityMobMixin(builder(Side.COMMON).unit(CompatibilityTier.Regular, "minecraft.EntityMobMixin")),
    EntityPlayerMPMixin(builder(Side.COMMON).unit(CompatibilityTier.Regular, "minecraft.EntityPlayerMPMixin")),
    ExtendedBlockStorageMixin(builder(Side.COMMON).unit(CompatibilityTier.Regular, "minecraft.ExtendedBlockStorageMixin")),
    WorldMixin(builder(Side.COMMON).unit(CompatibilityTier.InjectCancel, "minecraft.WorldMixin")),

    ClientChunkMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.ChunkMixin")),
    ClientChunkCacheMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.ChunkCacheMixin")),
    ClientEntityRendererMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.EntityRendererMixin")),
    ClientOpenGLHelperMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.OpenGLHelperMixin")),
    ClientRenderBlocksMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.RenderBlocksMixin")),
    ClientRenderingRegistryMixin(builder(Side.CLIENT).unit(CompatibilityTier.Regular, "minecraft.RenderingRegistryMixin")),
    ClientTessellatorMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.TessellatorMixin")),
    ClientWorldMixin(builder(Side.CLIENT).unit(CompatibilityTier.InjectCancel, "minecraft.WorldMixin"));

    public final MixinUnit[] units;
    public final Set<TargetedMod> targetedMods;
    private final Side side;

    Mixin(Builder builder) {
        this.units = builder.units.toArray(new MixinUnit[0]);
        this.targetedMods = builder.targetedMods;
        this.side = builder.side;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.COMMON
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }

    public String getBestAlternativeForTier(CompatibilityTier tier) {
        for (val unit: units) {
            if (unit.tier.isTierBetterThan(tier)) return unit.mixinClass;
        }
        throw new MixinException("Failed to retrieve mixin alternative for " + this.name() + " in mod " + ModInfo.MODID);
    }


    private static Builder builder(Side side) {
        return new Builder(side).target(VANILLA);
    }

    private static class Builder {
        public final ArrayList<MixinUnit> units = new ArrayList<>();
        public final Side side;
        public final Set<TargetedMod> targetedMods = new HashSet<>();

        public Builder(Side side) {
            this.side = side;
        }

        public Builder unit(CompatibilityTier tier, String mixinClass) {
            units.add(new MixinUnit(tier, side.name().toLowerCase() + "." + mixinClass));
            return this;
        }

        public Builder target(TargetedMod mod) {
            targetedMods.add(mod);
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
        COMMON,
        CLIENT,
        SERVER
    }
}

