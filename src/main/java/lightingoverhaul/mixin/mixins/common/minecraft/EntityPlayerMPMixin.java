package lightingoverhaul.mixin.mixins.common.minecraft;

import lightingoverhaul.server.PlayerManagerHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin {

    @Inject(method = "onUpdate",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V",
                     shift = At.Shift.AFTER,
                     ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    public void onUpdate(CallbackInfo ci, ArrayList<Chunk> arraylist) {
        PlayerManagerHelper.entityPlayerMP_onUpdate(arraylist, (EntityPlayerMP) (Object) this);
    }
}
