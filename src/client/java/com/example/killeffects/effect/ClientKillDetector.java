package com.example.killeffects.effect;

import com.example.killeffects.config.EffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Handles the vanilla kill notification sent to the client that scored the kill. */
public final class ClientKillDetector {
    private static final long POSITION_MEMORY_TICKS = 100;
    private static final Map<Integer, LastPosition> positions = new HashMap<>();
    private static ClientLevel trackedLevel;
    private static long tick;

    private ClientKillDetector() { }

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            reset();
            return;
        }
        if (trackedLevel != client.level) {
            reset();
            trackedLevel = client.level;
        }

        tick++;
        for (Player player : client.level.players()) {
            if (player == client.player) continue;
            positions.put(player.getId(), new LastPosition(BlockPos.containing(player.position()), tick));
        }
        Iterator<Map.Entry<Integer, LastPosition>> iterator = positions.entrySet().iterator();
        while (iterator.hasNext()) {
            if (tick - iterator.next().getValue().seenAtTick() > POSITION_MEMORY_TICKS) iterator.remove();
        }
    }

    /** Called by the mixin when vanilla tells this client it scored a player kill. */
    public static void onPlayerKill(Minecraft client, int victimEntityId) {
        if (client.player == null || client.level == null || !EffectConfig.enabled) return;
        Entity victim = client.level.getEntity(victimEntityId);
        BlockPos position = victim != null
                ? BlockPos.containing(victim.position())
                : positions.containsKey(victimEntityId) ? positions.get(victimEntityId).position() : null;
        if (position == null) return;

        int effectIndex = EffectManager.EFFECTS.indexOf(EffectManager.find(EffectConfig.selected));
        EffectManager.playKillEffect(client, effectIndex, position);
    }

    private static void reset() {
        trackedLevel = null;
        tick = 0;
        positions.clear();
    }

    private record LastPosition(BlockPos position, long seenAtTick) { }
}
