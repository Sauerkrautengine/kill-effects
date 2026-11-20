package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class TreasureReveal implements KillEffect {
 public String id(){return "treasure";} public String name(){return "Treasure Reveal";} public String category(){return "Funny";} public String description(){return "A 3D chest springs open and lifts a beam of loot into the air.";} public int duration(){return 70;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t<8)return;int n=Math.min(12,limit/12);for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.18),r=.35+(t%9)*.055;Particles.emit(w,(i%3==0?ParticleTypes.HAPPY_VILLAGER:ParticleTypes.WAX_ON),x+Math.cos(a)*r,y+.25+(t%16)*.055,z+Math.sin(a)*r,0,.04,0);}}
}
