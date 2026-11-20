package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class ArcaneBurst implements KillEffect {
 public String id(){return "arcane";} public String name(){return "Arcane Burst";} public String category(){return "Magical";} public String description(){return "Twin magic circles rotate while runic prisms rise around a pulse.";} public int duration(){return 58;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){int n=24;double r=.45+Math.min(t,28)*.035;for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.11);Particles.emit(w,ParticleTypes.ENCHANT,x+Math.cos(a)*r,y+.08,z+Math.sin(a)*r,-Math.sin(a)*.02,.025,Math.cos(a)*.02);}if(t==28)for(int i=0;i<Math.min(56,limit);i++){double a=Particles.angle(i,56,0);Particles.emit(w,ParticleTypes.WITCH,x,y+.3,z,Math.cos(a)*.3,.08,Math.sin(a)*.3);}}
}
