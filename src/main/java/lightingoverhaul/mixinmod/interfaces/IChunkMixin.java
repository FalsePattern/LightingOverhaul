package lightingoverhaul.mixinmod.interfaces;

public interface IChunkMixin {
    boolean canReallySeeTheSky(int x, int y, int z);

    int getRealSunColor(int x, int y, int z);
}
