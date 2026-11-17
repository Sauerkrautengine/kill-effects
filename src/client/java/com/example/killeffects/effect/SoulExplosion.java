package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class SoulExplosion implements KillEffect {
 public String id(){return "soul";} public String name(){return "Soul Explosion";} public String category(){return "Magical";} public String description(){return "Souls orbit, burst outward, then rise away.";} public int duration(){return 78;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t<18){for(int i=0;i<4;i++)Particles.emit(w,ParticleTypes.SOUL,x,y+.15,z,0,.12,0);}else if(t<38){int n=9;for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.13),r=.8;Particles.emit(w,i%2==0?ParticleTypes.SOUL:ParticleTypes.SOUL_FIRE_FLAME,x+Math.cos(a)*r,y+.8+Math.sin(a*2)*.4,z+Math.sin(a)*r,0,.025,0);}}else if(t==38){int n=Math.min(limit,180);for(int i=0;i<n;i++){double a=Particles.angle(i,n,i*.23),v=.12+(i%9)*.018;Particles.emit(w,i%3==0?ParticleTypes.SOUL:ParticleTypes.SOUL_FIRE_FLAME,x,y+.5,z,Math.cos(a)*v,.08+(i%7)*.025,Math.sin(a)*v);}}else if(t<68&&t%3==0)Particles.emit(w,ParticleTypes.SOUL,x,y+.25,z,0,.09,0);}
}
