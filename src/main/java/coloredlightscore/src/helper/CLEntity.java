package coloredlightscore.src.helper;

import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class CLEntity {
    public static int getBrightnessForRender(Entity instance, float f)
    {
        if (instance instanceof EntityFlameFX)
        {
            return 0x7fffffff;
        }

        int i = MathHelper.floor_double(instance.posX);
        int j = MathHelper.floor_double(instance.posZ);

        if (instance.worldObj.getBlock(i, 0, j) != null) {
          double d = (instance.boundingBox.maxY - instance.boundingBox.minY) * 0.66D;
          int k = MathHelper.floor_double(instance.posY - instance.yOffset + d);
          return instance.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
        }
        return 0;
    }
}
