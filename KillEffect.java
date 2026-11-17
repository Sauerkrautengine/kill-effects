package com.example.killeffects.effect;

import net.minecraft.client.multiplayer.ClientLevel;

public interface KillEffect {
    String id();
    String name();
    String category();
    String description();
    int duration();
    void tick(ClientLevel world, double x, double y, double z, int age, int particleLimit);
}
