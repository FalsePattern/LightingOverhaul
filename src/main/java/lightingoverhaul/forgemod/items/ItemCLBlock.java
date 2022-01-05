package lightingoverhaul.forgemod.items;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemCLBlock extends ItemBlock {

    public ItemCLBlock(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        return getUnlocalizedName() + itemstack.getItemDamage();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List dataList, boolean p_77624_4_) {
        if (itemStack.getItemDamage() == 0) {
            dataList.add(I18n.format("nolight.text"));
        }
    }

    @Override
    public int getMetadata(int par1) {
        return par1;
    }
}
