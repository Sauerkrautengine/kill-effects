package com.example.killeffects.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;

public final class MeteorStrike implements KillEffect {
    public String id() { return "meteor"; }
    public String name() { return "Meteor Strike"; }
    public String category() { return "Explosive"; }
    public String description() { return "A fiery descent, impact bloom and ground ring."; }
    public int duration() { return 64; }

    public void tick(ClientLevel world, double x, double y, double z, int age, int limit) {
        if (age < 25) {
            double height = 10.5 - Math.pow(age / 25.0, 2) * 9.55;
            Particles.emit(world, ParticleTypes.FLAME, x, y + height, z, 0, -.1, 0);
            for (int i = 0; i < 3; i++) {
                Particles.emit(world, ParticleTypes.SMOKE, x + (i - 1) * .16, y + height + .25,
                        z + (1 - i) * .12, (i - 1) * .012, -.025, (1 - i) * .01);
            }
            return;
        }
        if (age == 25) {
            Particles.emit(world, ParticleTypes.EXPLOSION_EMITTER, x, y + .12, z, 0, 0, 0);
            int count = Math.min(48, Math.max(12, limit / 4));
            for (int i = 0; i < count; i++) {
                double a = Particles.angle(i, count, 0), speed = .12 + (i % 5) * .045;
                Particles.emit(world, i % 4 == 0 ? ParticleTypes.SMOKE : ParticleTypes.FLAME,
                        x, y + .12, z, Math.cos(a) * speed, .06 + (i % 4) * .02, Math.sin(a) * speed);
            }
            return;
        }
        if (age < 48 && age % 3 == 0) {
            double radius = .3 + (age - 25) * .12;
            for (int i = 0; i < 8; i++) {
                double a = Particles.angle(i, 8, age * .035);
                Particles.emit(world, i % 3 == 0 ? ParticleTypes.SMOKE : ParticleTypes.FLAME,
                        x + Math.cos(a) * radius, y + .08, z + Math.sin(a) * radius,
                        Math.cos(a) * .018, .035, Math.sin(a) * .018);
            }
        }
    }
}
