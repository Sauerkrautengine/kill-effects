package com.example.killeffects.mixin;

import com.example.killeffects.effect.ClientKillDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handleHurtAnimation", at = @At("HEAD"))
    private void killeffects$recordConfirmedPlayerHit(ClientboundHurtAnimationPacket packet, CallbackInfo ci) {
        ClientKillDetector.onHurtAnimation(packet.id());
    }
}
