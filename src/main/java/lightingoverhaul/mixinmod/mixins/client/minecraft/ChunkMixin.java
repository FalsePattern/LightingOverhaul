package lightingoverhaul.mixinmod.mixins.client.minecraft;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
@SideOnly(Side.CLIENT)
public abstract class ChunkMixin {
    @Shadow public abstract int getTopFilledSegment();

    @Shadow public int heightMapMinimum;

    @Shadow public int[] precipitationHeightMap;

    @Shadow public int[] heightMap;

    @Shadow public boolean isModified;

    @Shadow public abstract Block getBlock(int p_150810_1_, int p_150810_2_, int p_150810_3_);

    @Shadow public abstract int func_150808_b(int p_150808_1_, int p_150808_2_, int p_150808_3_);

    @Inject(method = "generateHeightMap",
            at=@At("HEAD"),
            cancellable = true,
            require = 1)
    public void generateHeightMap(CallbackInfo ci) {
        ci.cancel();
        int i = getTopFilledSegment();
        this.heightMapMinimum = Integer.MAX_VALUE;
        for (byte b = 0; b < 16; b++) {
            for (byte b1 = 0; b1 < 16; b1++) {
                this.precipitationHeightMap[b + (b1 << 4)] = -999;
                for (int j = i + 16 - 1; j > 0; j--) {
                    if (!is_translucent_for_relightBlock(b, j - 1, b1)) {
                        this.heightMap[b1 << 4 | b] = j;
                        if (j < this.heightMapMinimum)
                            this.heightMapMinimum = j;
                        break;
                    }
                }
            }
        }
        this.isModified = true;
    }

    private boolean is_translucent_for_relightBlock(int x, int y, int z) {
        if (getBlock(x, y, z) instanceof BlockStainedGlass) {
            return false;
        }
        return func_150808_b(x, y, z) == 0;
    }

}
