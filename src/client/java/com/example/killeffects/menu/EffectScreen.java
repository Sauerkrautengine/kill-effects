package com.example.killeffects.menu;

import com.example.killeffects.config.EffectConfig;
import com.example.killeffects.effect.EffectManager;
import com.example.killeffects.effect.KillEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EffectScreen extends Screen {
    private String category = "All Effects";
    private int page;

    public EffectScreen() { super(Component.literal("Kill Effects")); }

    @Override protected void init() {
        clearWidgets();
        String[] categories = {"All Effects", "Magical", "Funny", "Explosive", "Classic"};
        int left = (width - 500) / 2;
        for (int i = 0; i < categories.length; i++) {
            String selectedCategory = categories[i];
            addRenderableWidget(Button.builder(Component.literal(selectedCategory), button -> {
                category = selectedCategory;
                page = 0;
                init();
            }).bounds(left + i * 101, 34, 98, 20).build());
        }

        var effects = EffectManager.EFFECTS.stream()
                .filter(effect -> category.equals("All Effects") || effect.category().equals(category)).toList();
        int pageSize = 8;
        int start = page * pageSize;
        for (int slot = 0; slot < pageSize && start + slot < effects.size(); slot++) {
            KillEffect effect = effects.get(start + slot);
            int column = slot % 2;
            int row = slot / 2;
            int bx = left + column * 254;
            int by = 72 + row * 52;
            String selectedMark = EffectConfig.selected.equals(effect.id()) ? "  ✓" : "";

            addRenderableWidget(Button.builder(Component.literal(icon(effect.id()) + "  " + effect.name() + selectedMark), button -> {
                EffectConfig.selected = effect.id();
                EffectConfig.save();
                init();
            }).bounds(bx, by, 246, 22).build());

            addRenderableWidget(Button.builder(Component.literal(
                    EffectConfig.isEffectEnabled(effect.id()) ? "Disable effect" : "Enable effect"), button -> {
                EffectConfig.toggleEffect(effect.id());
                init();
            }).bounds(bx + 73, by + 25, 100, 19).build());
        }

        if (start + pageSize < effects.size()) {
            addRenderableWidget(Button.builder(Component.literal("Next ›"), button -> { page++; init(); })
                    .bounds(left + 408, 282, 90, 20).build());
        }
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("‹ Previous"), button -> { page--; init(); })
                    .bounds(left + 304, 282, 98, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal(EffectConfig.enabled ? "Effects ON" : "Effects OFF"), button -> {
            EffectConfig.enabled = !EffectConfig.enabled;
            EffectConfig.save();
            init();
        }).bounds(left + 45, 316, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Particles " + EffectConfig.particleLimit), button -> {
            EffectConfig.particleLimit = EffectConfig.particleLimit >= 600 ? 60 : EffectConfig.particleLimit + 60;
            EffectConfig.save();
            init();
        }).bounds(left + 140, 316, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal(EffectConfig.sounds ? "Sound ON" : "Sound OFF"), button -> {
            EffectConfig.sounds = !EffectConfig.sounds;
            EffectConfig.save();
            init();
        }).bounds(left + 265, 316, 85, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(left + 355, 316, 75, 20).build());
    }

    private String icon(String id) {
        return switch (id) {
            case "soul" -> "✧"; case "meteor" -> "☄"; case "void" -> "◉"; case "anvil" -> "▣";
            case "cards" -> "▤"; case "thunder" -> "ϟ"; case "firework" -> "✹"; case "arcane" -> "◇";
            case "tornado" -> "〰"; default -> "★";
        };
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int left = (width - 500) / 2;
        graphics.text(font, "KILL EFFECTS", left, 12, 0xFFE9F4FF, true);
        graphics.text(font, "Your selected effect plays when you score a direct PvP kill", left, 55, 0xFF9FB1C8, true);
        KillEffect selected = EffectManager.find(EffectConfig.selected);
        graphics.text(font, selected.name() + " — " + selected.description(), left, 292, 0xFFB7C8DD, true);
    }

    @Override public boolean isPauseScreen() { return false; }
}
