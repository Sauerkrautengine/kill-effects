package com.example.killeffects;

import com.example.killeffects.config.EffectConfig;
import com.example.killeffects.effect.EffectManager;
import com.example.killeffects.menu.EffectScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;

public final class KillEffectsClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    @Override public void onInitializeClient() {
        EffectConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("killeffects", "main"));
        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killeffects.open_menu", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) client.setScreen(new EffectScreen());
            EffectManager.tick(client);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal("killeffects")
                        .executes(context -> {
                            Minecraft.getInstance().setScreen(new EffectScreen());
                            return 1;
                        })));
    }
}
