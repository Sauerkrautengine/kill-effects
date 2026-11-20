package com.example.killeffects;

import com.example.killeffects.config.EffectConfig;
import com.example.killeffects.effect.EffectManager;
import com.example.killeffects.effect.ClientKillDetector;
import com.example.killeffects.menu.EffectScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class KillEffectsClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    @Override public void onInitializeClient() {
        EffectConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("killeffects", "main"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killeffects.open_menu", InputConstants.KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) client.gui.setScreen(new EffectScreen());
            ClientKillDetector.tick(client);
            EffectManager.tick(client);
        });
        // Signal 1: remember the players you hit, so their death can be credited to you
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() && player == Minecraft.getInstance().player && entity instanceof Player victim) {
                ClientKillDetector.onAttack(victim);
            }
            return InteractionResult.PASS;
        });
        // Signal 2: death messages such as "X was shot by <you>"
        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                ClientKillDetector.onGameMessage(Minecraft.getInstance(), message));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("killeffects")
                        .executes(context -> {
                            Minecraft.getInstance().gui.setScreen(new EffectScreen());
                            return 1;
                        })));
    }
}
