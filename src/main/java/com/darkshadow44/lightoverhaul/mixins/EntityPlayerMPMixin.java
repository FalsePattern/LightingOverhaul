package com.darkshadow44.lightoverhaul.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import coloredlightscore.server.PlayerManagerHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin {

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "net.minecraft.network.NetHandlerPlayServer.sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1, shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    void constructS26PacketMapChunkBulk(CallbackInfo callback, ArrayList<Chunk> chunks) {
        PlayerManagerHelper.entityPlayerMP_onUpdate(chunks, (EntityPlayerMP) (Object) this);
    }
}
