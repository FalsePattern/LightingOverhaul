package com.lightingoverhaul.mixinmod.mixins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lightingoverhaul.coremod.server.PlayerManagerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {

    public EntityPlayerMPMixin(World p_i45324_1_, GameProfile p_i45324_2_) {
        super(p_i45324_1_, p_i45324_2_);
    }

    @SuppressWarnings("rawtypes")
    @Shadow
    public List loadedChunks;

    @Shadow
    public abstract WorldServer getServerForPlayer();

    @Shadow
    private int field_147101_bU;

    @Shadow
    public ItemInWorldManager theItemInWorldManager;

    @SuppressWarnings("rawtypes")
    @Shadow
    private List destroyedItemsNetCache;

    @Shadow
    public NetHandlerPlayServer playerNetServerHandler;

    @Shadow
    abstract void func_147097_b(TileEntity p_147097_1_);

    /**
     * @author darkshadow44
     * @reason Call entityPlayerMP_onUpdate, inject doesn't work due to a bug.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Overwrite
    public void onUpdate() {
        this.theItemInWorldManager.updateBlockRemoving();
        --this.field_147101_bU;

        if (this.hurtResistantTime > 0) {
            --this.hurtResistantTime;
        }

        this.openContainer.detectAndSendChanges();

        if (!this.worldObj.isRemote && !ForgeHooks.canInteractWith(this, this.openContainer)) {
            this.closeScreen();
            this.openContainer = this.inventoryContainer;
        }

        while (!this.destroyedItemsNetCache.isEmpty()) {
            int i = Math.min(this.destroyedItemsNetCache.size(), 127);
            int[] aint = new int[i];
            Iterator iterator = this.destroyedItemsNetCache.iterator();
            int j = 0;

            while (iterator.hasNext() && j < i) {
                aint[j++] = ((Integer) iterator.next()).intValue();
                iterator.remove();
            }

            this.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(aint));
        }

        if (!this.loadedChunks.isEmpty()) {
            ArrayList arraylist = new ArrayList();
            Iterator iterator1 = this.loadedChunks.iterator();
            ArrayList arraylist1 = new ArrayList();
            Chunk chunk;

            while (iterator1.hasNext() && arraylist.size() < S26PacketMapChunkBulk.func_149258_c()) {
                ChunkCoordIntPair chunkcoordintpair = (ChunkCoordIntPair) iterator1.next();

                if (chunkcoordintpair != null) {
                    if (this.worldObj.blockExists(chunkcoordintpair.chunkXPos << 4, 0, chunkcoordintpair.chunkZPos << 4)) {
                        chunk = this.worldObj.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);

                        if (chunk.func_150802_k()) {
                            arraylist.add(chunk);
                            arraylist1.addAll(((WorldServer) this.worldObj).func_147486_a(chunkcoordintpair.chunkXPos * 16, 0, chunkcoordintpair.chunkZPos * 16, chunkcoordintpair.chunkXPos * 16 + 15,
                                    256, chunkcoordintpair.chunkZPos * 16 + 15));
                            // BugFix: 16 makes it load an extra chunk, which isn't associated with a
                            // player, which makes it not unload unless a player walks near it.
                            iterator1.remove();
                        }
                    }
                } else {
                    iterator1.remove();
                }
            }

            if (!arraylist.isEmpty()) {
                this.playerNetServerHandler.sendPacket(new S26PacketMapChunkBulk(arraylist));
                PlayerManagerHelper.entityPlayerMP_onUpdate(arraylist, (EntityPlayerMP) (Object) this); // ADDED
                Iterator iterator2 = arraylist1.iterator();

                while (iterator2.hasNext()) {
                    TileEntity tileentity = (TileEntity) iterator2.next();
                    this.func_147097_b(tileentity);
                }

                iterator2 = arraylist.iterator();

                while (iterator2.hasNext()) {
                    chunk = (Chunk) iterator2.next();
                    this.getServerForPlayer().getEntityTracker().func_85172_a((EntityPlayerMP) (Object) this, chunk);
                    MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.Watch(chunk.getChunkCoordIntPair(), (EntityPlayerMP) (Object) this));
                }
            }
        }
    }
}
