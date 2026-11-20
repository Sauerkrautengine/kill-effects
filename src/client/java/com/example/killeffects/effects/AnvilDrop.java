package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
public final class AnvilDrop implements KillEffect {
 public String id(){return "anvil";} public String name(){return "Anvil Drop";} public String category(){return "Classic";} public String description(){return "A forged iron anvil drops from the sky and sends a ground ring outward.";} public int duration(){return 62;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){ if(t<20&&t%2==0) Particles.emit(w,ParticleTypes.CLOUD,x,y+5-t*.24,z,0,-.08,0); if(t==20){Particles.emit(w,ParticleTypes.EXPLOSION_EMITTER,x,y,z,0,0,0); for(int i=0;i<Math.min(36,limit);i++){double a=Particles.angle(i,36,0); Particles.emit(w,ParticleTypes.CRIT,x,y+.1,z,Math.cos(a)*.45,.16,Math.sin(a)*.45);}} }
}
