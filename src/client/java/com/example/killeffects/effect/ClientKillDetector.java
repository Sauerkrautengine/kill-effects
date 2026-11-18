package com.example.killeffects.effect;

import com.example.killeffects.config.EffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-only kill attribution using the local player's authoritative player-kills stat.
 * The death position is matched to the most recently observed remote player death.
 */
public final class ClientKillDetector {
    private static final Stat<Identifier> PLAYER_KILLS = Stats.CUSTOM.get(Stats.PLAYER_KILLS);
    private static final long DEATH_MEMORY_TICKS = 100;
    private static final long STAT_MATCH_TICKS = 40;
    private static final Map<UUID, DeathCandidate> recentDeaths = new HashMap<>();

    private static ClientLevel trackedLevel;
    private static int lastKillCount = -1;
    private static int pendingKillEvents;
    private static long pendingUntilTick;
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
            lastKillCount = currentKillCount(client);
            return;
        }

        tick++;
        collectRecentDeaths(client.level, client.player);
        recentDeaths.values().removeIf(death -> tick - death.observedAtTick() > DEATH_MEMORY_TICKS);

        int currentKillCount = currentKillCount(client);
        if (currentKillCount < lastKillCount) {
            lastKillCount = currentKillCount;
            pendingKillEvents = 0;
        } else if (currentKillCount > lastKillCount) {
            pendingKillEvents = Math.min(10, pendingKillEvents + currentKillCount - lastKillCount);
            pendingUntilTick = tick + STAT_MATCH_TICKS;
            lastKillCount = currentKillCount;
        }

        while (pendingKillEvents > 0 && !recentDeaths.isEmpty()) {
            DeathCandidate candidate = mostRecentDeath();
            if (candidate == null) break;
            recentDeaths.remove(candidate.playerId());
            int effectIndex = EffectManager.EFFECTS.indexOf(EffectManager.find(EffectConfig.selected));
            EffectManager.playKillEffect(client, effectIndex, candidate.position());
            pendingKillEvents--;
        }

        if (pendingKillEvents > 0 && tick > pendingUntilTick) pendingKillEvents = 0;
    }

    private static void collectRecentDeaths(ClientLevel level, Player localPlayer) {
        for (Player other : level.players()) {
            if (other == localPlayer || !other.isDeadOrDying()) continue;
            recentDeaths.putIfAbsent(other.getUUID(), new DeathCandidate(
                    other.getUUID(), BlockPos.containing(other.position()), tick));
        }
    }

    private static DeathCandidate mostRecentDeath() {
        DeathCandidate newest = null;
        for (DeathCandidate candidate : recentDeaths.values()) {
            if (newest == null || candidate.observedAtTick() > newest.observedAtTick()) newest = candidate;
        }
        return newest;
    }

    private static int currentKillCount(Minecraft client) {
        return client.player.getStats().getValue(PLAYER_KILLS);
    }

    private static void reset() {
        trackedLevel = null;
        lastKillCount = -1;
        pendingKillEvents = 0;
        pendingUntilTick = 0;
        tick = 0;
        recentDeaths.clear();
    }

    private record DeathCandidate(UUID playerId, BlockPos position, long observedAtTick) { }
}
