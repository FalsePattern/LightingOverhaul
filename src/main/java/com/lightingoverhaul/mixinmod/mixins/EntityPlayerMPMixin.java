package com.lightingoverhaul.mixinmod.mixins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lightingoverhaul.coremod.server.PlayerManagerHelper;
import org.spongepowered.asm.mixin.Final;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin {

    @Inject(method = "onUpdate",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V",
                     shift = At.Shift.AFTER,
                     ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onUpdate(CallbackInfo ci, ArrayList<Chunk> arraylist) {
        PlayerManagerHelper.entityPlayerMP_onUpdate(arraylist, (EntityPlayerMP) (Object) this);
    }
}
