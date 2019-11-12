package com.darkshadow44.lightoverhaul.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class TestMixin {
    @Inject(at = @At("HEAD"), method = "startGame()V")
    private void init(CallbackInfo info) {
        for (int i = 0; i < 100; i++)
        {
            System.out.println("########################");
        }
        System.out.println("This line is printed by an example mod mixin!");
        for (int i = 0; i < 100; i++)
        {
            System.out.println("########################");
        }
    }
}
