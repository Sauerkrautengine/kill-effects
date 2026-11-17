package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class MeteorStrike implements KillEffect {
 public String id(){return "meteor";} public String name(){return "Meteor Strike";} public String category(){return "Explosive";} public String description(){return "A fiery descent, impact bloom and ground ring.";} public int duration(){return 64;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t<28){double h=9-t*.3;Particles.emit(w,ParticleTypes.FLAME,x,y+h,z,0,-.08,0);for(int i=0;i<3;i++)Particles.emit(w,ParticleTypes.SMOKE,x+(i-1)*.12,y+h+.2,z,0,-.02,0);}else if(t==28){Particles.emit(w,ParticleTypes.EXPLOSION_EMITTER,x,y+.1,z,0,0,0);}else if(t<50){int n=32;double r=(t-28)*.12;for(int i=0;i<n;i++){double a=Particles.angle(i,n,0);Particles.emit(w,i%2==0?ParticleTypes.FLAME:ParticleTypes.SMOKE,x+Math.cos(a)*r,y+.08,z+Math.sin(a)*r,0,.015,0);}}}
}
