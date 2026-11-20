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
import java.util.Locale;
import java.util.Map;

/** Client-only, best-effort detection for kills credited to the local player. */
public final class ClientKillDetector {
    private static final long HIT_WINDOW_TICKS = 100;
    private static final long POSITION_MEMORY_TICKS = 120;
    private static final long CONFIRMED_HIT_WINDOW_TICKS = 100;
    private static final long VANISH_FALLBACK_TICKS = 40;
    private static final long DEDUPE_TICKS = 60;
    private static final float LOW_HEALTH = 8.0F;

    private static final Map<Integer, Hit> hits = new HashMap<>();
    private static final Map<Integer, LastPosition> positions = new HashMap<>();
    private static final Map<String, LastPosition> positionsByName = new HashMap<>();
    private static final Map<String, Long> recentKills = new HashMap<>();
    private static ClientLevel trackedLevel;
    private static long tick;

    private ClientKillDetector() { }

    public static void onAttack(Player victim) {
        Hit hit = hits.computeIfAbsent(victim.getId(), ignored -> new Hit(victim.getName().getString(), tick));
        hit.lastHitTick = tick;
        hit.position = BlockPos.containing(victim.position());
    }

    /** Called from the vanilla hurt-animation packet; it still works when HP metadata is hidden. */
    public static void onHurtAnimation(int entityId) {
        Hit hit = hits.get(entityId);
        if (hit != null && tick - hit.lastHitTick <= HIT_WINDOW_TICKS) hit.lastConfirmedHurtTick = tick;
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
            BlockPos currentPosition = BlockPos.containing(player.position());
            Hit hit = hits.get(player.getId());
            LastPosition previous = positions.get(player.getId());
            if (hit != null && previous != null && tick - hit.lastHitTick <= HIT_WINDOW_TICKS
                    && tick - hit.lastConfirmedHurtTick <= CONFIRMED_HIT_WINDOW_TICKS
                    && distanceSquared(previous.position(), currentPosition) >= 24L * 24L) {
                trigger(client, hit.name, previous.position());
                hit.completed = true;
            }
            LastPosition last = new LastPosition(currentPosition, tick);
            positions.put(player.getId(), last);
            positionsByName.put(player.getName().getString(), last);
        }
        positions.values().removeIf(position -> tick - position.seenAtTick() > POSITION_MEMORY_TICKS);
        positionsByName.values().removeIf(position -> tick - position.seenAtTick() > POSITION_MEMORY_TICKS);
        recentKills.values().removeIf(killTick -> tick - killTick > DEDUPE_TICKS);

        Iterator<Map.Entry<Integer, Hit>> iterator = hits.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Hit> entry = iterator.next();
            Hit hit = entry.getValue();
            if (hit.completed) {
                iterator.remove();
                continue;
            }
            if (tick - hit.lastHitTick > HIT_WINDOW_TICKS) {
                iterator.remove();
                continue;
            }

            Entity entity = client.level.getEntity(entry.getKey());
            if (entity instanceof LivingEntity living && !living.isRemoved()) {
                hit.position = BlockPos.containing(living.position());
                hit.lastHealth = living.getHealth();
                if (living.isDeadOrDying()) {
                    trigger(client, hit.name, hit.position);
                    iterator.remove();
                }
                continue;
            }

            LastPosition last = positions.get(entry.getKey());
            if (last != null) hit.position = last.position();
            boolean recentlyConfirmedHurt = tick - hit.lastConfirmedHurtTick <= CONFIRMED_HIT_WINDOW_TICKS;
            boolean vanishedSoonAfterAttack = tick - hit.lastHitTick <= VANISH_FALLBACK_TICKS;
            if (hit.position != null && (recentlyConfirmedHurt || hit.lastHealth <= LOW_HEALTH || vanishedSoonAfterAttack)) {
                trigger(client, hit.name, hit.position);
            }
            iterator.remove();
        }
    }

    /** Handles vanilla translated death messages and common server-generated text messages. */
    public static void onGameMessage(Minecraft client, Component message) {
        if (client.player == null || client.level == null) return;
        String me = client.player.getName().getString();

        if (message.getContents() instanceof TranslatableContents contents
                && contents.getKey().startsWith("death.")) {
            Object[] args = contents.getArgs();
            if (args.length >= 2) {
                String victim = text(args[0]);
                String killer = text(args[1]);
                if (killer.equalsIgnoreCase(me) && !victim.equalsIgnoreCase(me)) {
                    LastPosition position = positionsByName.get(victim);
                    if (position != null) trigger(client, victim, position.position());
                }
            }
            return;
        }

        String raw = message.getString();
        String lower = raw.toLowerCase(Locale.ROOT);
        if (!containsKillWord(lower)) return;

        for (Map.Entry<String, LastPosition> entry : positionsByName.entrySet()) {
            String victim = entry.getKey();
            if (victim.equalsIgnoreCase(me) || !lower.contains(victim.toLowerCase(Locale.ROOT))) continue;
            boolean namesKiller = lower.contains(me.toLowerCase(Locale.ROOT));
            boolean saysYouKilled = lower.contains("du hast") || lower.contains("you killed")
                    || lower.contains("you eliminated") || lower.contains("kill +");
            if (namesKiller || saysYouKilled) trigger(client, victim, entry.getValue().position());
        }

        // Some game modes only show a generic "Kill +1" or "Du hast einen Kill" actionbar.
        if ((lower.contains("kill +") || lower.contains("kill erhalten") || lower.contains("kill erzielt"))
                && (lower.contains("du") || lower.contains("you"))) {
            Hit newest = null;
            for (Hit hit : hits.values()) {
                if (tick - hit.lastHitTick <= HIT_WINDOW_TICKS
                        && (newest == null || hit.lastHitTick > newest.lastHitTick)) newest = hit;
            }
            if (newest != null && newest.position != null) trigger(client, newest.name, newest.position);
        }
    }

    private static boolean containsKillWord(String message) {
        return message.contains("getötet") || message.contains("tötete") || message.contains("killed")
                || message.contains("kill") || message.contains("eliminiert") || message.contains("eliminated")
                || message.contains("besiegt") || message.contains("defeated") || message.contains("frag");
    }

    private static long distanceSquared(BlockPos a, BlockPos b) {
        long dx = (long) a.getX() - b.getX();
        long dy = (long) a.getY() - b.getY();
        long dz = (long) a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static String text(Object arg) {
        return arg instanceof Component component ? component.getString() : String.valueOf(arg);
    }

    private static void trigger(Minecraft client, String victimName, BlockPos position) {
        if (!EffectConfig.enabled || position == null) return;
        Long previous = recentKills.get(victimName);
        if (previous != null && tick - previous < DEDUPE_TICKS) return;
        recentKills.put(victimName, tick);
        int effectIndex = EffectManager.EFFECTS.indexOf(EffectManager.find(EffectConfig.selected));
        EffectManager.playKillEffect(client, effectIndex, position);
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
        long lastConfirmedHurtTick = Long.MIN_VALUE / 2;
        float lastHealth = 20.0F;
        BlockPos position;
        boolean completed;

        Hit(String name, long tick) {
            this.name = name;
            this.lastHitTick = tick;
        }
    }

    private record LastPosition(BlockPos position, long seenAtTick) { }
}
