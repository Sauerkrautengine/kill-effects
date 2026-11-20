package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class ParticleTornado implements KillEffect {
 public String id(){return "tornado";} public String name(){return "Particle Tornado";} public String category(){return "Explosive";} public String description(){return "Three twisting 3D ribbons spiral into a tapered vortex.";} public int duration(){return 72;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){int layers=8;for(int j=0;j<layers;j++){double h=(t*.085+j*.43)%4.6,a=t*.27+j*.91,r=.18+h*.14;Particles.emit(w,ParticleTypes.CLOUD,x+Math.cos(a)*r,y+h,z+Math.sin(a)*r,-Math.sin(a)*.03,.05,Math.cos(a)*.03);}}
}
