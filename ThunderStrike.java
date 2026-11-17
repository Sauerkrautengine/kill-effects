package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class ThunderStrike implements KillEffect {
 public String id(){return "thunder";} public String name(){return "Thunder Strike";} public String category(){return "Classic";} public String description(){return "A branching electric flash without fire.";} public int duration(){return 30;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){if(t==1||t==3){for(int j=0;j<7;j++){double h=j*.38;Particles.emit(w,ParticleTypes.ELECTRIC_SPARK,x+Math.sin(j*2.1)*.28,y+h,z+Math.cos(j*1.7)*.28,0,0,0);}} if(t==4)for(int i=0;i<Math.min(30,limit);i++){double a=Particles.angle(i,30,0);Particles.emit(w,ParticleTypes.ELECTRIC_SPARK,x,y+.15,z,Math.cos(a)*.32,.04,Math.sin(a)*.32);}}
}
