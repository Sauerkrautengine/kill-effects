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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import com.example.killeffects.effect.VisualEffectRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;

public final class KillEffectsClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    @Override public void onInitializeClient() {
        EffectConfig.load();
        com.example.killeffects.effect.VisualEffectRenderer.initialize();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("killeffects", "main"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killeffects.open_menu", InputConstants.KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) client.gui.setScreen(new EffectScreen());
            ClientKillDetector.tick(client);
            EffectManager.tick(client);
        });
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() && player == Minecraft.getInstance().player && entity instanceof Player victim) {
                ClientKillDetector.onAttack(victim);
            }
            return InteractionResult.PASS;
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) ->
                ClientKillDetector.onGameMessage(Minecraft.getInstance(), message));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, boundChatType, timestamp) ->
                ClientKillDetector.onGameMessage(Minecraft.getInstance(), message));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
            // Keep the long command for discoverability, and the short alias to
            // avoid collisions with server plugins using the same root name.
            dispatcher.register(commandTree("killeffects"));
            dispatcher.register(commandTree("ke"));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> commandTree(String root) {
        return ClientCommands.literal(root)
                .executes(context -> {
                    Minecraft.getInstance().gui.setScreen(new EffectScreen());
                    return 1;
                })
                .then(ClientCommands.literal("test")
                        .executes(context -> playTest(context.getSource(), null))
                        .then(ClientCommands.argument("effect", StringArgumentType.word())
                                .executes(context -> playTest(context.getSource(), StringArgumentType.getString(context, "effect")))))
                .then(ClientCommands.literal("debug").executes(context -> {
                    context.getSource().sendFeedback(Component.literal("[KillEffects] 3D renderer: " + VisualEffectRenderer.stats()));
                    return 1;
                }))
                .then(ClientCommands.literal("list").executes(context -> {
                    String ids = String.join(", ", EffectManager.EFFECTS.stream().map(effect -> effect.id()).toList());
                    context.getSource().sendFeedback(Component.literal("[KillEffects] Effects: " + ids));
                    return 1;
                }));
    }

    private static int playTest(FabricClientCommandSource source, String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            source.sendError(Component.literal("[KillEffects] Join a world before testing effects."));
            return 0;
        }
        var effect = id == null ? EffectManager.find(EffectConfig.selected)
                : EffectManager.EFFECTS.stream().filter(candidate -> candidate.id().equalsIgnoreCase(id)).findFirst().orElse(null);
        if (effect == null) {
            source.sendError(Component.literal("[KillEffects] Unknown effect. Use /ke list."));
            return 0;
        }
        if (!EffectConfig.enabled || !EffectConfig.isEffectEnabled(effect.id())) {
            source.sendError(Component.literal("[KillEffects] Enable this effect in the menu before testing it."));
            return 0;
        }
        BlockPos spot = mc.player.blockPosition().relative(mc.player.getDirection(), 4);
        EffectManager.playKillEffect(mc, EffectManager.EFFECTS.indexOf(effect), spot);
        source.sendFeedback(Component.literal("[KillEffects] Playing 3D test: " + effect.name()));
        return 1;
    }
}
