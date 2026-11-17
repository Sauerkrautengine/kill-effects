package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class FireworkCelebration implements KillEffect {
 public String id(){return "firework";} public String name(){return "Firework Celebration";} public String category(){return "Explosive";} public String description(){return "A rising star with two colorful bursts.";} public int duration(){return 62;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t<24){Particles.emit(w,ParticleTypes.FIREWORK,x,y+t*.13,z,0,.05,0);return;}if(t==24||t==38){int n=Math.min(40,limit/3);for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.13),v=.22+(i%4)*.035;Particles.emit(w,(i%2==0?ParticleTypes.FIREWORK:ParticleTypes.END_ROD),x,y+3,z,Math.cos(a)*v,.14+(i%5)*.025,Math.sin(a)*v);}}}
}
