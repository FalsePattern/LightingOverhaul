package lightingoverhaul.coremod.mixin.interfaces;

public interface IBlockMixin {
    void setMetadataLightValue(int metadata, int lightValue);
    void setLightValue(int newValue);
    int getLightValue_INTERNAL();
}
