package com.example.killeffects.effect;
import net.minecraft.client.multiplayer.ClientLevel; import net.minecraft.core.particles.ParticleTypes;
public final class VoidPortal implements KillEffect {
 public String id(){return "void";} public String name(){return "Void Portal";} public String category(){return "Magical";} public String description(){return "A rotating 3D portal frame opens, spirals, and collapses to a point.";} public int duration(){return 90;}
 public void tick(ClientLevel w,double x,double y,double z,int t,int limit){double expand=t<35?.2+t*.018:Math.max(.04,.83-(t-35)*.021);int n=24;for(int i=0;i<n;i++){double a=Particles.angle(i,n,t*.08),r=expand;Particles.emit(w,i%5==0?ParticleTypes.END_ROD:ParticleTypes.REVERSE_PORTAL,x+Math.cos(a)*r,y+.1+Math.sin(a)*.55,z+Math.sin(a)*r,-Math.cos(a)*.035,.01,-Math.sin(a)*.035);}if(t==78)for(int i=0;i<16;i++){double a=Particles.angle(i,16,0);Particles.emit(w,ParticleTypes.PORTAL,x,y+.3,z,Math.cos(a)*.17,.04,Math.sin(a)*.17);}}
}
