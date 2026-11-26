package com.example.killeffects.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;

public final class AnvilDrop implements KillEffect {
    public String id() { return "anvil"; }
    public String name() { return "Anvil Drop"; }
    public String category() { return "Classic"; }
    public String description() { return "A forged iron anvil drops from the sky and sends a ground ring outward."; }
    public int duration() { return 62; }

    public void tick(ClientLevel world, double x, double y, double z, int age, int limit) {
        if (age < 25 && age % 2 == 0) {
            double height = 8.8 - age * .34;
            Particles.emit(world, ParticleTypes.CLOUD, x, y + height, z, 0, -.035, 0);
            Particles.emit(world, ParticleTypes.SMOKE, x - .18, y + height + .1, z + .12, -.01, -.025, .01);
            Particles.emit(world, ParticleTypes.SMOKE, x + .16, y + height + .1, z - .1, .01, -.025, -.01);
        }
        if (age == 25) {
            Particles.emit(world, ParticleTypes.EXPLOSION_EMITTER, x, y + .15, z, 0, 0, 0);
            int count = Math.min(48, Math.max(12, limit / 3));
            for (int i = 0; i < count; i++) {
                double a = Particles.angle(i, count, 0), speed = .18 + (i % 5) * .055;
                Particles.emit(world, i % 3 == 0 ? ParticleTypes.CRIT : ParticleTypes.CLOUD,
                        x, y + .12, z, Math.cos(a) * speed, .04 + (i % 4) * .025, Math.sin(a) * speed);
            }
        }
        if (age > 25 && age < 48 && age % 3 == 0) {
            double radius = .25 + (age - 25) * .13;
            for (int i = 0; i < 12; i++) {
                double a = Particles.angle(i, 12, age * .025);
                Particles.emit(world, ParticleTypes.CLOUD, x + Math.cos(a) * radius, y + .08,
                        z + Math.sin(a) * radius, Math.cos(a) * .015, .01, Math.sin(a) * .015);
            }
        }
    }
}
