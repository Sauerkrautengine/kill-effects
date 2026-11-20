package com.example.killeffects.effect;

import com.example.killeffects.config.EffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Detects kills scored by the local player, purely on the client.
 *
 * Vanilla does NOT tell the killer about a kill: the "player combat kill" packet goes to the player who died.
 * So two independent signals are used instead:
 *  1. Hit tracking: players you hit are watched for a few seconds. If one of them dies (or disappears while
 *     almost dead) the kill is credited to you.
 *  2. Death messages: a message like "X was slain by <you>" or "X was shot by <you>" also credits you
 *     (this covers arrows and other indirect kills).
 * A kill is only counted once, even if both signals fire.
 */
public final class ClientKillDetector {
    private static final long HIT_WINDOW_TICKS = 100;
    private static final long POSITION_MEMORY_TICKS = 100;
    private static final long DEDUPE_TICKS = 60;
    private static final float LOW_HEALTH = 8.0F;

    private static final Map<Integer, Hit> hits = new HashMap<>();
    private static final Map<Integer, LastPosition> positions = new HashMap<>();
    private static final Map<String, LastPosition> positionsByName = new HashMap<>();
    private static final Map<String, Long> recentKills = new HashMap<>();
    private static ClientLevel trackedLevel;
    private static long tick;

    private ClientKillDetector() { }

    /** Called when the local player attacks another player. */
    public static void onAttack(Player victim) {
        Hit hit = hits.get(victim.getId());
        if (hit == null) hits.put(victim.getId(), new Hit(tick, victim.getName().getString()));
        else hit.lastHitTick = tick;
    }

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
            LastPosition last = new LastPosition(BlockPos.containing(player.position()), tick);
            positions.put(player.getId(), last);
            positionsByName.put(player.getName().getString(), last);
        }
        positions.values().removeIf(p -> tick - p.seenAtTick() > POSITION_MEMORY_TICKS);
        positionsByName.values().removeIf(p -> tick - p.seenAtTick() > POSITION_MEMORY_TICKS);
        recentKills.values().removeIf(t -> tick - t > DEDUPE_TICKS);

        Iterator<Map.Entry<Integer, Hit>> iterator = hits.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Hit> entry = iterator.next();
            Hit hit = entry.getValue();
            if (tick - hit.lastHitTick > HIT_WINDOW_TICKS) {
                iterator.remove();
                continue;
            }
            Entity entity = client.level.getEntity(entry.getKey());
            if (entity instanceof LivingEntity living && !living.isRemoved()) {
                hit.lastHealth = living.getHealth();
                if (living.isDeadOrDying()) {
                    trigger(client, hit.name, BlockPos.containing(living.position()));
                    iterator.remove();
                }
            } else {
                // The victim vanished: count it only if it was almost dead (not a teleport or a log-out)
                LastPosition last = positions.get(entry.getKey());
                if (hit.lastHealth <= LOW_HEALTH && last != null) trigger(client, hit.name, last.position());
                iterator.remove();
            }
        }
    }

    /** Called for every chat/game message; credits kills announced as "<victim> ... by <you>". */
    public static void onGameMessage(Minecraft client, Component message) {
        if (client.player == null || client.level == null) return;
        if (!(message.getContents() instanceof TranslatableContents contents)) return;
        if (!contents.getKey().startsWith("death.")) return;
        Object[] args = contents.getArgs();
        if (args.length < 2) return;

        String victim = text(args[0]);
        String killer = text(args[1]);
        String me = client.player.getName().getString();
        if (!killer.equals(me) || victim.equals(me)) return;

        LastPosition last = positionsByName.get(victim);
        if (last == null) return; // not a player we have seen (for example a mob)
        trigger(client, victim, last.position());
    }

    private static void trigger(Minecraft client, String victimName, BlockPos position) {
        if (!EffectConfig.enabled) return;
        Long previous = recentKills.get(victimName);
        if (previous != null && tick - previous < DEDUPE_TICKS) return;
        recentKills.put(victimName, tick);

        int effectIndex = EffectManager.EFFECTS.indexOf(EffectManager.find(EffectConfig.selected));
        EffectManager.playKillEffect(client, effectIndex, position);
    }

    private static String text(Object arg) {
        return arg instanceof Component component ? component.getString() : String.valueOf(arg);
    }

    private static void reset() {
        trackedLevel = null;
        tick = 0;
        hits.clear();
        positions.clear();
        positionsByName.clear();
        recentKills.clear();
    }

    private static final class Hit {
        final String name;
        long lastHitTick;
        float lastHealth = 20.0F;

        Hit(long tick, String name) {
            this.name = name;
            this.lastHitTick = tick;
        }
    }

    private record LastPosition(BlockPos position, long seenAtTick) { }
}
