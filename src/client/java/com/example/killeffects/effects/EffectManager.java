package com.example.killeffects.effect;

import com.example.killeffects.config.EffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.List;

public final class EffectManager {
    public static final List<KillEffect> EFFECTS = List.of(new AnvilDrop(), new CardExplosion(), new ThunderStrike(),
            new FireworkCelebration(), new ArcaneBurst(), new ParticleTornado(), new TreasureReveal(),
            new SoulExplosion(), new MeteorStrike(), new VoidPortal());
    private static KillEffect playing;
    private static int age;
    private static double x,y,z;
    private EffectManager() {}
    public static KillEffect find(String id) { return EFFECTS.stream().filter(e -> e.id().equals(id)).findFirst().orElse(EFFECTS.getFirst()); }
    public static void playKillEffect(Minecraft client, int effectIndex, BlockPos position) {
        if (effectIndex < 0 || effectIndex >= EFFECTS.size()) return;
        KillEffect effect = EFFECTS.get(effectIndex);
        if (!EffectConfig.enabled || !EffectConfig.isEffectEnabled(effect.id())) return;
        if (client.player == null || client.level == null) return;
        x=position.getX()+0.5; y=position.getY(); z=position.getZ()+0.5; playing=effect; age=0;
        if (usesGeometry(effect.id())) VisualEffectRenderer.start(effect.id(), x, y, z, effect.duration());
        else VisualEffectRenderer.stop();
        if (EffectConfig.sounds) client.level.playLocalSound(x,y,z,SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.AMBIENT,0.45F,1.25F,false);
    }
    public static void tick(Minecraft client) {
        if (playing==null || client.level==null) return;
        if (!EffectConfig.enabled) { VisualEffectRenderer.stop(); playing=null; return; }
        ClientLevel level=client.level;
        if (usesGeometry(playing.id())) VisualEffectRenderer.tick();
        playing.tick(level,x,y,z,age,EffectConfig.particleLimit);
        age++;
        if (age>=playing.duration()) { VisualEffectRenderer.stop(); playing=null; }
    }
    private static boolean usesGeometry(String id) { return !id.equals("soul") && !id.equals("meteor"); }
}
