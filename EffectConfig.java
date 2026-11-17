package com.example.killeffects.config;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/** Small, crash-safe client preference store. */
public final class EffectConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("killeffects.properties");
    public static String selected = "anvil";
    public static boolean enabled = true;
    public static boolean sounds = true;
    public static int particleLimit = 180;
    private static final Set<String> disabled = new HashSet<>();
    private EffectConfig() {}
    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        Properties p = new Properties();
        try (var in = Files.newInputStream(FILE)) {
            p.load(in); selected = p.getProperty("selected", "anvil");
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            sounds = Boolean.parseBoolean(p.getProperty("sounds", "true"));
            particleLimit = Math.max(24, Math.min(600, Integer.parseInt(p.getProperty("particleLimit", "180"))));
            disabled.clear();
            String raw = p.getProperty("disabled", "");
            if (!raw.isBlank()) for (String id : raw.split(",")) disabled.add(id.trim());
        } catch (IOException | NumberFormatException ignored) { }
    }
    public static void save() {
        Properties p = new Properties(); p.setProperty("selected", selected);
        p.setProperty("enabled", Boolean.toString(enabled)); p.setProperty("sounds", Boolean.toString(sounds));
        p.setProperty("particleLimit", Integer.toString(particleLimit));
        p.setProperty("disabled", String.join(",", disabled));
        try { Files.createDirectories(FILE.getParent()); try (var out = Files.newOutputStream(FILE)) { p.store(out, "KillEffects preferences"); } }
        catch (IOException ignored) { }
    }
    public static boolean isEffectEnabled(String id) { return !disabled.contains(id); }
    public static void toggleEffect(String id) { if (!disabled.add(id)) disabled.remove(id); save(); }
}
