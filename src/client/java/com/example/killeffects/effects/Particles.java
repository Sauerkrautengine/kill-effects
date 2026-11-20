package com.example.killeffects.effect;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;

final class Particles {
    private Particles() {}
    static void emit(ClientLevel w, ParticleOptions p, double x,double y,double z,double dx,double dy,double dz) {
        w.addParticle(p, x,y,z,dx,dy,dz);
    }
    static double angle(int i,int count,double phase) { return phase + Math.PI*2.0*i/count; }
}
