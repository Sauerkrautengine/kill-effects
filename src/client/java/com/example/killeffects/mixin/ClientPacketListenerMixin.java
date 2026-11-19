package com.example.killeffects.mixin;

import com.example.killeffects.effect.ClientKillDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerCombatKill", at = @At("HEAD"))
    private void killeffects$playOnScoredKill(ClientboundPlayerCombatKillPacket packet, CallbackInfo ci) {
        ClientKillDetector.onPlayerKill(Minecraft.getInstance(), packet.playerId());
    }
}
