package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class CardExplosion implements KillEffect {
 public String id(){return "cards";} public String name(){return "Card Explosion";} public String category(){return "Funny";} public String description(){return "A pinwheel of sharp card-like sparks.";} public int duration(){return 56;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t>32)return;int n=Math.min(24,limit/4);for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.22+i*.7),r=.25+t*.035; Particles.emit(w,(i%3==0?ParticleTypes.CRIT:ParticleTypes.END_ROD),x+Math.cos(a)*r,y+.35+Math.sin(t*.24+i)*.5,z+Math.sin(a)*r,Math.cos(a)*.12,.02,Math.sin(a)*.12);}}
}
