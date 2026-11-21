package com.example.killeffects.menu;

import com.example.killeffects.config.EffectConfig;
import com.example.killeffects.effect.EffectManager;
import com.example.killeffects.effect.KillEffect;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** A compact, restrained selection screen built around Minecraft's normal controls. */
public final class EffectScreen extends Screen {
    private static final int PANEL_WIDTH = 600;
    private static final int PAGE_SIZE = 6;
    private static final String[] CATEGORIES = {"All Effects", "Magical", "Funny", "Explosive", "Classic"};
    private String category = CATEGORIES[0];
    private int page;
    private int panelLeft;

    public EffectScreen() { super(Component.literal("Kill Effects")); }

    @Override
    protected void init() {
        clearWidgets();
        panelLeft = (width - PANEL_WIDTH) / 2;

        int tabWidth = 108;
        int tabGap = 8;
        int tabsLeft = panelLeft + (PANEL_WIDTH - (tabWidth * CATEGORIES.length + tabGap * (CATEGORIES.length - 1))) / 2;
        for (int i = 0; i < CATEGORIES.length; i++) {
            String selectedCategory = CATEGORIES[i];
            String label = selectedCategory.equals(category) ? "• " + selectedCategory : selectedCategory;
            addRenderableWidget(Button.builder(Component.literal(label), button -> {
                category = selectedCategory;
                page = 0;
                init();
            }).bounds(tabsLeft + i * (tabWidth + tabGap), 66, tabWidth, 20).build());
        }

        List<KillEffect> effects = visibleEffects();
        int start = page * PAGE_SIZE;
        int cardWidth = (PANEL_WIDTH - 36) / 2;
        for (int slot = 0; slot < PAGE_SIZE && start + slot < effects.size(); slot++) {
            KillEffect effect = effects.get(start + slot);
            int column = slot % 2;
            int row = slot / 2;
            int bx = panelLeft + 12 + column * (cardWidth + 12);
            int by = 101 + row * 75;
            boolean selected = EffectConfig.selected.equals(effect.id());
            String label = (selected ? "✓  " : "   ") + icon(effect.id()) + "  " + effect.name();

            addRenderableWidget(Button.builder(Component.literal(label), button -> {
                EffectConfig.selected = effect.id();
                EffectConfig.save();
                init();
            }).bounds(bx + 7, by + 6, cardWidth - 14, 22).build());

            addRenderableWidget(Button.builder(Component.literal(
                    EffectConfig.isEffectEnabled(effect.id()) ? "ON" : "OFF"), button -> {
                EffectConfig.toggleEffect(effect.id());
                init();
            }).bounds(bx + cardWidth - 55, by + 39, 46, 18).build());
        }

        if (start + PAGE_SIZE < effects.size()) {
            addRenderableWidget(Button.builder(Component.literal("Next"), button -> { page++; init(); })
                    .bounds(panelLeft + PANEL_WIDTH - 93, 332, 80, 20).build());
        }
        if (page > 0) {
            addRenderableWidget(Button.builder(Component.literal("Previous"), button -> { page--; init(); })
                    .bounds(panelLeft + 13, 332, 80, 20).build());
        }

        addRenderableWidget(Button.builder(Component.literal(EffectConfig.enabled ? "Effects: ON" : "Effects: OFF"), button -> {
            EffectConfig.enabled = !EffectConfig.enabled;
            EffectConfig.save();
            init();
        }).bounds(panelLeft + 12, 371, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Limit: " + EffectConfig.particleLimit), button -> {
            EffectConfig.particleLimit = EffectConfig.particleLimit >= 600 ? 60 : EffectConfig.particleLimit + 60;
            EffectConfig.save();
            init();
        }).bounds(panelLeft + 120, 371, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal(EffectConfig.sounds ? "Sound: ON" : "Sound: OFF"), button -> {
            EffectConfig.sounds = !EffectConfig.sounds;
            EffectConfig.save();
            init();
        }).bounds(panelLeft + 228, 371, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(panelLeft + PANEL_WIDTH - 88, 371, 76, 20).build());
    }

    private List<KillEffect> visibleEffects() {
        return EffectManager.EFFECTS.stream()
                .filter(effect -> category.equals(CATEGORIES[0]) || effect.category().equals(category))
                .toList();
    }

    private String icon(String id) {
        return switch (id) {
            case "soul" -> "◇";
            case "meteor" -> "*";
            case "void" -> "O";
            case "anvil" -> "▣";
            case "cards" -> "▤";
            case "thunder" -> "ϟ";
            case "firework" -> "+";
            case "arcane" -> "⌘";
            case "tornado" -> "~";
            default -> "◆";
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int top = 8;
        int panelRight = panelLeft + PANEL_WIDTH;
        graphics.fillGradient(0, 0, width, height, 0x7010161D, 0xB0080D12);
        graphics.fill(panelLeft - 1, top - 1, panelRight + 1, 419, 0xE411171D);
        graphics.fill(panelLeft, top, panelRight, 419, 0xE90D1218);
        graphics.fill(panelLeft, top, panelRight, top + 2, 0xFF7CB6AA);
        graphics.fill(panelLeft + 12, 94, panelRight - 12, 95, 0xFF34434A);
        graphics.fill(panelLeft + 12, 360, panelRight - 12, 361, 0xFF34434A);

        List<KillEffect> effects = visibleEffects();
        int start = page * PAGE_SIZE;
        int cardWidth = (PANEL_WIDTH - 36) / 2;
        for (int slot = 0; slot < PAGE_SIZE && start + slot < effects.size(); slot++) {
            KillEffect effect = effects.get(start + slot);
            int x = panelLeft + 12 + (slot % 2) * (cardWidth + 12);
            int y = 101 + (slot / 2) * 75;
            boolean selected = EffectConfig.selected.equals(effect.id());
            boolean enabled = EffectConfig.isEffectEnabled(effect.id());
            graphics.fill(x, y, x + cardWidth, y + 67, selected ? 0xFF24332F : 0xFF171F25);
            graphics.fill(x, y, x + 2, y + 67, selected ? 0xFF83C8B5 : (enabled ? 0xFF53646B : 0xFF343D42));
            graphics.fill(x + 8, y + 32, x + cardWidth - 8, y + 33, 0xFF303B40);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(font, "KILL EFFECTS", panelLeft + 14, 19, 0xFFF0F1E8, false);
        graphics.text(font, "10 effects  /  local visual effects", panelLeft + PANEL_WIDTH - 190, 20, 0xFFAAB7B3, false);
        graphics.text(font, "Choose one effect. Only your client renders it; the world stays unchanged.",
                panelLeft + 14, 40, 0xFF9DAAA8, false);

        List<KillEffect> effects = visibleEffects();
        int start = page * PAGE_SIZE;
        int cardWidth = (PANEL_WIDTH - 36) / 2;
        for (int slot = 0; slot < PAGE_SIZE && start + slot < effects.size(); slot++) {
            KillEffect effect = effects.get(start + slot);
            int x = panelLeft + 21 + (slot % 2) * (cardWidth + 12);
            int y = 101 + (slot / 2) * 75;
            String description = effect.description();
            if (description.length() > 35) description = description.substring(0, 32) + "...";
            graphics.text(font, description, x, y + 40, 0xFFB4BFBC, false);
        }

        KillEffect selected = EffectManager.find(EffectConfig.selected);
        graphics.text(font, selected.name() + " selected", panelLeft + 14, 348, 0xFFD6E1DC, false);
        graphics.text(font, "Page " + (page + 1), panelLeft + PANEL_WIDTH / 2 - 18, 337, 0xFF9DAAA8, false);
    }

    @Override public boolean isPauseScreen() { return false; }
}
