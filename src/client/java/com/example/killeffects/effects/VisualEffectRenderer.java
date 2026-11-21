package com.example.killeffects.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Draws the larger effects as animated world geometry; particles are reserved for the two effects the user liked. */
public final class VisualEffectRenderer {
    private static final RenderPipeline PIPELINE = RenderPipelines.DEBUG_FILLED_BOX;
    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "KillEffects visual meshes", RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static volatile Scene active;
    private static volatile Scene extracted;
    private static Matrix4f matrix;
    private static VertexConsumer vertices;
    private static StagedVertexBuffer.Draw currentDraw;
    private static int emittedQuads;

    private VisualEffectRenderer() { }

    public static void initialize() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> extracted = active);
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(VisualEffectRenderer::prepare);
        LevelRenderEvents.END_MAIN.register(context -> BUFFER.endFrame());
    }

    public static void start(String id, double x, double y, double z, int duration) {
        active = new Scene(id, x, y, z, duration, 0);
    }

    public static void tick() {
        if (active == null) return;
        int nextAge = active.age() + 1;
        active = nextAge >= active.duration() ? null
                : new Scene(active.id(), active.x(), active.y(), active.z(), active.duration(), nextAge);
    }

    public static void stop() { active = null; extracted = null; }

    private static void prepare(LevelTerrainRenderContext context) {
        currentDraw = null;
        if (extracted == null) return;
        VertexFormat format = PIPELINE.getVertexFormatBinding(0);
        if (format == null) return;
        PrimitiveTopology topology = PIPELINE.getPrimitiveTopology();
        var sorting = topology == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null;
        currentDraw = BUFFER.appendDraw(format, topology, sorting);
        Vec3 camera = context.levelState().cameraRenderState.pos;
        matrix = new Matrix4f().translate((float) -camera.x, (float) -camera.y, (float) -camera.z);
        vertices = BUFFER.getVertexBuilder(currentDraw);
        emittedQuads = 0;
        render(extracted);
        if (emittedQuads == 0) currentDraw = null;
        BUFFER.upload();
    }

    public static void draw(RenderPass pass) {
        if (currentDraw == null) return;
        StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(currentDraw);
        if (info == null) return;
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        pass.pushDebugGroup(() -> "KillEffects animated geometry");
        pass.setPipeline(RenderSystem.getCompiledPipeline(PIPELINE));
        RenderSystem.bindDefaultUniforms(pass);
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, info.vertexBuffer().slice());
        pass.setIndexBuffer(info.indexBuffer(), info.indexType());
        pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        pass.popDebugGroup();
    }

    public static void close() { BUFFER.close(); }

    private static void render(Scene s) {
        switch (s.id()) {
            case "anvil" -> anvil(s);
            case "cards" -> cards(s);
            case "thunder" -> thunder(s);
            case "firework" -> fireworks(s);
            case "arcane" -> arcane(s);
            case "tornado" -> tornado(s);
            case "treasure" -> treasure(s);
            case "void" -> voidPortal(s);
        }
    }

    private static boolean room() { return emittedQuads < 1500; }
    private static void quad(double[] a, double[] b, double[] c, double[] d, float r, float g, float bl, float alpha) {
        if (!room()) return;
        vertex(a, r, g, bl, alpha); vertex(b, r, g, bl, alpha); vertex(c, r, g, bl, alpha); vertex(d, r, g, bl, alpha);
        emittedQuads++;
    }
    private static void vertex(double[] p, float r, float g, float b, float a) {
        vertices.addVertex(matrix, (float)p[0], (float)p[1], (float)p[2]).setColor(r, g, b, a);
    }
    private static void cube(double x, double y, double z, double sx, double sy, double sz, float r, float g, float b) {
        double x0=x-sx/2,x1=x+sx/2,y0=y-sy/2,y1=y+sy/2,z0=z-sz/2,z1=z+sz/2;
        quad(p(x0,y0,z1),p(x1,y0,z1),p(x1,y1,z1),p(x0,y1,z1),r,g,b,1);
        quad(p(x1,y0,z0),p(x0,y0,z0),p(x0,y1,z0),p(x1,y1,z0),r*.72f,g*.72f,b*.72f,1);
        quad(p(x0,y0,z0),p(x0,y0,z1),p(x0,y1,z1),p(x0,y1,z0),r*.82f,g*.82f,b*.82f,1);
        quad(p(x1,y0,z1),p(x1,y0,z0),p(x1,y1,z0),p(x1,y1,z1),r*.9f,g*.9f,b*.9f,1);
        quad(p(x0,y1,z1),p(x1,y1,z1),p(x1,y1,z0),p(x0,y1,z0),Math.min(1,r*1.2f),Math.min(1,g*1.2f),Math.min(1,b*1.2f),1);
        quad(p(x0,y0,z0),p(x1,y0,z0),p(x1,y0,z1),p(x0,y0,z1),r*.55f,g*.55f,b*.55f,1);
    }
    private static void ring(double x,double y,double z,double inner,double outer,int segments,float r,float g,float b) {
        for(int i=0;i<segments&&room();i++) {
            double a=Math.PI*2*i/segments, n=Math.PI*2*(i+1)/segments;
            quad(p(x+Math.cos(a)*inner,y,z+Math.sin(a)*inner),p(x+Math.cos(a)*outer,y,z+Math.sin(a)*outer),
                    p(x+Math.cos(n)*outer,y,z+Math.sin(n)*outer),p(x+Math.cos(n)*inner,y,z+Math.sin(n)*inner),r,g,b,1);
        }
    }
    private static void ovalRing(double x,double y,double z,double rx,double ry,double width,double yaw,int segments,float r,float g,float b) {
        for(int i=0;i<segments&&room();i++) {
            double a=Math.PI*2*i/segments,n=Math.PI*2*(i+1)/segments;
            double[] p0=ovalPoint(x,y,z,rx,ry,a,yaw,width),p1=ovalPoint(x,y,z,rx,ry,a,yaw,-width),
                    p2=ovalPoint(x,y,z,rx,ry,n,yaw,-width),p3=ovalPoint(x,y,z,rx,ry,n,yaw,width);
            quad(p0,p1,p2,p3,r,g,b,1);
        }
    }
    private static double[] ovalPoint(double x,double y,double z,double rx,double ry,double a,double yaw,double w) {
        double px=Math.cos(a)*rx,py=Math.sin(a)*ry;
        return p(x+px*Math.cos(yaw)-w*Math.sin(yaw),y+py,z+px*Math.sin(yaw)+w*Math.cos(yaw));
    }
    private static void ribbon(double[] a,double[] b,double[] c,double[] d,float r,float g,float bl) { quad(a,b,c,d,r,g,bl,1); }
    private static double[] p(double x,double y,double z) { return new double[]{x,y,z}; }

    private static void anvil(Scene s) {
        double t=s.age(), x=s.x(), y=s.y(), z=s.z();
        if(t<26) {
            double fall=10.5-Math.pow(t/26.0,2)*9.6;
            double yaw=t*.035;
            rotatedCube(x,y+fall,z,1.45,.34,.82,yaw,.32f,.36f,.43f);
            rotatedCube(x,y+fall+.28,z,1.08,.25,.58,yaw,.48f,.53f,.62f);
            rotatedCube(x-.73,y+fall-.11,z,.56,.25,.52,yaw,.34f,.39f,.48f);
            for(int i=0;i<4;i++) cube(x+Math.cos(i*1.57)*.8,y+fall-1.1-(t%5)*.1,z+Math.sin(i*1.57)*.8,.08,.8,.08,.48f,.52f,.62f);
            if(t>12) ring(x,y+.06,z,.2,.55+(t-12)*.035,24,.45f,.5f,.58f);
        } else if(t<58) {
            double f=(t-26)/32;
            ring(x,y+.05,z,.05,Math.max(.1,4.1*(1-f)),56,.76f,.79f,.84f);
            if(t<33) {
                rotatedCube(x,y+.15,z,1.45,.34,.82,0,.38f,.42f,.5f);
                rotatedCube(x,y+.43,z,1.08,.25,.58,0,.52f,.56f,.64f);
                rotatedCube(x-.73,y+.04,z,.56,.25,.52,0,.4f,.44f,.52f);
            }
            for(int i=0;i<12;i++) {
                double a=Particles.angle(i,12,0), r=1.1+f*1.5;
                cube(x+Math.cos(a)*r,y+.12+Math.sin(t*.16+i)*.15,z+Math.sin(a)*r,.13,.13,.13,.54f,.59f,.68f);
            }
        }
    }
    private static void rotatedCube(double x,double y,double z,double sx,double sy,double sz,double yaw,float r,float g,float b) {
        double ca=Math.cos(yaw),sa=Math.sin(yaw);
        double[][] local={p(-sx/2,-sy/2,-sz/2),p(sx/2,-sy/2,-sz/2),p(sx/2,sy/2,-sz/2),p(-sx/2,sy/2,-sz/2),
                p(-sx/2,-sy/2,sz/2),p(sx/2,-sy/2,sz/2),p(sx/2,sy/2,sz/2),p(-sx/2,sy/2,sz/2)};
        double[][] out=new double[8][3];
        for(int i=0;i<8;i++){double dx=local[i][0],dz=local[i][2];out[i]=p(x+dx*ca-dz*sa,y+local[i][1],z+dx*sa+dz*ca);}
        quad(out[4],out[5],out[6],out[7],Math.min(1,r*1.2f),Math.min(1,g*1.2f),Math.min(1,b*1.2f),1);
        quad(out[1],out[0],out[3],out[2],r*.68f,g*.68f,b*.68f,1);
        quad(out[0],out[4],out[7],out[3],r*.82f,g*.82f,b*.82f,1);
        quad(out[5],out[1],out[2],out[6],r*.9f,g*.9f,b*.9f,1);
        quad(out[3],out[7],out[6],out[2],Math.min(1,r*1.12f),Math.min(1,g*1.12f),Math.min(1,b*1.12f),1);
        quad(out[0],out[1],out[5],out[4],r*.55f,g*.55f,b*.55f,1);
    }

    private static void cards(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z();
        int count=11;
        for(int i=0;i<count;i++) {
            double phase=i*Math.PI*2/count, progress=Math.min(1,t/34.0), radius=.18+progress*(1.5+(i%3)*.16);
            double cx=x+Math.cos(phase+t*.025)*radius,cy=y+1.1+Math.sin(t*.13+i)*.8+progress*.7,cz=z+Math.sin(phase+t*.025)*radius;
            double yaw=phase+t*(.075+(i%2)*.03), roll=Math.sin(t*.11+i)*.45;
            card(cx,cy,cz,.45,.7,yaw,roll,(i%3==0)?new float[]{.92f,.22f,.3f}:i%3==1?new float[]{.16f,.62f,.9f}:new float[]{.95f,.72f,.18f});
            if(t>23) runeDiamond(cx,cy,cz,.1+(i%2)*.04,yaw,1,.97f,.83f);
        }
        if(t<24) ring(x,y+.08,z,.65, .72,48,.97f,.8f,.24f);
        if(t>40) ring(x,y+.04,z,.2,Math.max(.21,2.6-(t-40)*.075),48,.97f,.53f,.17f);
    }
    private static void card(double x,double y,double z,double w,double h,double yaw,double roll,float[] color) {
        double ca=Math.cos(yaw),sa=Math.sin(yaw), cr=Math.cos(roll),sr=Math.sin(roll);
        double[][] corners={{-w/2,-h/2},{w/2,-h/2},{w/2,h/2},{-w/2,h/2}};double[][] out=new double[4][3];
        for(int i=0;i<4;i++){double xx=corners[i][0],yy=corners[i][1];double ry=yy*cr,rx=xx+yy*sr;out[i]=p(x+rx*ca,y+ry,z+rx*sa);}
        quad(out[0],out[1],out[2],out[3],color[0],color[1],color[2],1);
        quad(out[3],out[2],out[1],out[0],.98f,.91f,.68f,1);
    }

    private static void thunder(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z();
        if(t<7) {
            ring(x,y+.04,z,.2,.34+t*.045,48,.18f,.82f,1);
            return;
        }
        double fade=t<20?1:Math.max(.12,1-(t-20)/20.0);
        for(int bolt=0;bolt<4;bolt++) {
            double bx=x+Math.sin(bolt*2.4)*(.5+bolt*.13),bz=z+Math.cos(bolt*1.9)*(.5+bolt*.12);
            double prevX=bx,prevZ=bz;
            int segments=9;
            for(int j=1;j<=segments;j++) {
                double py=y+10.5-j*1.12;
                double offset=Math.sin(j*3.7+bolt*7.1)*(.3+(j*.02));
                double nx=bx+offset,nz=bz+Math.cos(j*2.1+bolt)*.25;
                lightningSegment(prevX,py+1.12,prevZ,nx,py,nz,.11*fade,.12f,.72f,1,fade);
                lightningSegment(prevX,py+1.12,prevZ,nx,py,nz,.045*fade,.78f,.97f,1,fade);
                if(j==4&&bolt<2) lightningSegment(nx,py,nz,nx+Math.sin(bolt+1)*.85,py-1.4,nz+Math.cos(bolt+2)*.7,.065,.3f,.78f,1,fade);
                prevX=nx;prevZ=nz;
            }
        }
        ring(x,y+.08,z,.1,1.4+Math.max(0,t-7)*.11,56,.22f,.76f,1);
    }
    private static void lightningSegment(double x1,double y1,double z1,double x2,double y2,double z2,double width,float r,float g,float b,double alpha) {
        double dx=x2-x1,dz=z2-z1,len=Math.max(.001,Math.sqrt(dx*dx+dz*dz)),ox=-dz/len*width,oz=dx/len*width;
        quad(p(x1+ox,y1,z1+oz),p(x1-ox,y1,z1-oz),p(x2-ox,y2,z2-oz),p(x2+ox,y2,z2+oz),r,g,b,(float)alpha);
    }

    private static void fireworks(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z();
        if(t<22) {
            double h=t*.24;
            rocket(x,y+h,z,t*.12);
            ring(x,y+.08,z,.15,.26+t*.015,32,.98f,.36f,.12f);
        } else {
            double elapsed=t-22;
            int burst=elapsed<26?0:1;double local=elapsed<26?elapsed:elapsed-26;
            if(elapsed>52)return;
            double radius=.12+local*.12,fade=Math.max(.15,1-local/27.0);
            burstStar(x,y+5.2-burst*1.5,z,radius,burst,fade);
            if(local<17) sphereRays(x,y+5.2-burst*1.5,z,radius,16,burst,fade);
        }
    }
    private static void rocket(double x,double y,double z,double yaw) {
        rotatedCube(x,y+.38,z,.18,.7,.18,yaw,.95f,.31f,.08f);
        rotatedCube(x,y+.78,z,.28,.15,.28,yaw,1,.75f,.18f);
        for(int i=0;i<4;i++) {double a=i*Math.PI/2;cube(x+Math.cos(a)*.16,y+.12,z+Math.sin(a)*.16,.12,.2,.12,.3f,.65f,1);}
    }
    private static void burstStar(double x,double y,double z,double radius,int burst,double fade) {
        int rays=12;
        for(int i=0;i<rays;i++) {
            double a=Math.PI*2*i/rays+burst*.13, elevation=((i%3)-1)*.48;
            double ex=x+Math.cos(a)*radius,ez=z+Math.sin(a)*radius,ey=y+Math.sin(elevation)*radius;
            float r=burst==0?1:.35f,g=burst==0?.28f:.75f,b=burst==0?.12f:1;
            lightningSegment(x,y,z,ex,ey,ez,.055*fade,r,g,b,fade);
            cube(ex,ey,ez,.09*fade,.09*fade,.09*fade,Math.min(1,r+.15f),Math.min(1,g+.15f),Math.min(1,b+.15f));
        }
    }
    private static void sphereRays(double x,double y,double z,double radius,int count,int color,double fade) {
        for(int i=0;i<count;i++) {
            double a=i*2.399963,b=Math.acos(1-2*(i+.5)/count);
            double ex=x+Math.cos(a)*Math.sin(b)*radius*1.7,ey=y+Math.cos(b)*radius*1.7,ez=z+Math.sin(a)*Math.sin(b)*radius*1.7;
            lightningSegment(x,y,z,ex,ey,ez,.035*fade,color==0?1:.35f,color==0?.43f:.75f,1,fade);
        }
    }

    private static void arcane(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z(),pulse=.7+.25*Math.sin(t*.16);
        ring(x,y+.08,z,.9*pulse,1.02*pulse,72,.48f,.18f,1);
        ring(x,y+.13,z,.55*pulse,.62*pulse,64,.16f,.76f,1);
        for(int i=0;i<6;i++) {
            double a=Particles.angle(i,6,-t*.035),rx=Math.cos(a)*.8,rz=Math.sin(a)*.8;
            runeDiamond(x+rx,y+.18+Math.sin(t*.09+i)*.24,z+rz,.22,a+t*.05,1,.74f,.23f);
            // Small three-dimensional runic pillars orbit around the ring.
            rotatedCube(x+rx*1.2,y+.55+Math.sin(t*.1+i)*.18,z+rz*1.2,.08,.58,.08,a,.65f,.29f,1);
        }
        if(t>17&&t<50) {
            double shell=(t-17)*.09;
            for(int i=0;i<8;i++){double a=Particles.angle(i,8,t*.018);runeDiamond(x+Math.cos(a)*shell,y+.45+Math.sin(a*2)*.15,z+Math.sin(a)*shell,.13,a, .25f,.95f,1);}
            if(t%3==0) ring(x,y+.08,z,.08,shell,48,.5f,.35f,.9f);
        }
    }
    private static void runeDiamond(double x,double y,double z,double size,double yaw,float r,float g,float b) {
        double ca=Math.cos(yaw),sa=Math.sin(yaw);double[][] pts=new double[4][3];
        double[][] base={{0,size},{size*.62,0},{0,-size},{-size*.62,0}};
        for(int i=0;i<4;i++){double px=base[i][0],py=base[i][1];pts[i]=p(x+px*ca,y+py,z+px*sa);}
        lightningSegment(pts[0][0],pts[0][1],pts[0][2],pts[1][0],pts[1][1],pts[1][2],.035,r,g,b,1);
        lightningSegment(pts[1][0],pts[1][1],pts[1][2],pts[2][0],pts[2][1],pts[2][2],.035,r,g,b,1);
        lightningSegment(pts[2][0],pts[2][1],pts[2][2],pts[3][0],pts[3][1],pts[3][2],.035,r,g,b,1);
        lightningSegment(pts[3][0],pts[3][1],pts[3][2],pts[0][0],pts[0][1],pts[0][2],.035,r,g,b,1);
    }

    private static void tornado(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z(),height=5.4;
        ring(x,y+.06,z,.25,.4,48,.16f,.78f,.98f);
        for(int strand=0;strand<3;strand++) {
            int steps=38;double phase=strand*Math.PI*2/3+t*.08;
            for(int i=0;i<steps&&room();i++) {
                double h=height*i/(steps-1),angle=phase+h*1.9-t*.095;
                double r=.22+h*.29,nh=height*(i+1)/(steps-1),na=phase+nh*1.9-t*.095,nr=.22+nh*.29;
                double[] a=p(x+Math.cos(angle)*r,y+h,z+Math.sin(angle)*r);
                double[] b=p(x+Math.cos(angle)*(r+.16),y+h,z+Math.sin(angle)*(r+.16));
                double[] c=p(x+Math.cos(na)*(nr+.16),y+nh,z+Math.sin(na)*(nr+.16));
                double[] d=p(x+Math.cos(na)*nr,y+nh,z+Math.sin(na)*nr);
                ribbon(a,b,c,d,strand==0?.18f:.58f,strand==1?.7f:.28f,1);
            }
        }
        for(int layer=0;layer<5;layer++) {
            double h=layer*1.05+(t*.055%1.05),r=.24+h*.28;
            ring(x,y+h,z,r-.07,r+.08,48,.2f,.78f,1);
        }
    }

    private static void treasure(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z();
        if(t<13) {
            for(int i=0;i<8;i++){double a=Particles.angle(i,8,t*.025);runeDiamond(x+Math.cos(a)*.8,y+.12,z+Math.sin(a)*.8,.16,a,1,.68f,.08f);}
            return;
        }
        double open=Math.min(1,(t-13)/16.0),beam=t<48?1:Math.max(.1,1-(t-48)/18.0);
        // Lifted treasure chest: base, gold rim, raised lid and front latch.
        cube(x,y+.35,z,.95,.48,.66,.42f,.18f,.055f);
        cube(x,y+.58,z,.99,.09,.69,.97f,.59f,.12f);
        cube(x,y+.89+open*.28,z-.22,.98,.48,.22,.5f,.23f,.06f);
        cube(x,y+.89+open*.28,z+.06,.98,.48,.22,.57f,.27f,.065f);
        cube(x,y+.89+open*.28,z-.08,.98,.48,.18,.62f,.32f,.08f);
        cube(x,y+.47,z+.34,.16,.2,.08,1,.78f,.18f);
        cube(x,y+.43,z+.4,.09,.1,.04,1,.9f,.36f);
        // A solid emerald-and-gold loot column emerges from the open chest.
        for(int i=0;i<5;i++) {
            double h=.95+i*.48,angle=i*2.4+t*.04,r=.22+i*.1;
            cube(x+Math.cos(angle)*r,y+1+h*open,z+Math.sin(angle)*r,.18,.18,.18,i%2==0?.95f:.18f,i%2==0?.67f:.9f,.12f);
        }
        for(int i=0;i<4;i++) {double a=Particles.angle(i,4,t*.012);runeDiamond(x+Math.cos(a)*.7,y+1.1+open*1.2,z+Math.sin(a)*.7,.2,a,1,.78f,.18f);}
        ring(x,y+.05,z,.4,1.05,56,1,.61f,.12f);
        if(open>.7) {
            for(int i=0;i<4;i++){double a=Particles.angle(i,4,t*.02);lightningSegment(x,y+.8,z,x+Math.cos(a)*.55,y+2.6,z+Math.sin(a)*.55,.09*beam,1,.75f,.21f,beam);}
        }
    }

    private static void voidPortal(Scene s) {
        double t=s.age(),x=s.x(),y=s.y(),z=s.z();
        double open=t<24?t/24.0:t<64?1:Math.max(.02,1-(t-64)/25.0),yaw=t*.018;
        double rx=1.15*open,ry=2.05*open;
        if(open<=.03)return;
        ovalRing(x,y+1.9,z,rx,ry,.16,yaw,72,.23f,.06f,.5f);
        ovalRing(x,y+1.9,z,rx*.84,ry*.91,.07,yaw,72,.86f,.12f,1);
        ovalRing(x,y+1.9,z,rx*1.12,ry*1.1,.055,yaw,72,.14f,.82f,1);
        // Interlocked vertical spirals create the portal's depth without a flat particle sheet.
        for(int strand=0;strand<3;strand++) {
            int steps=42;double phase=strand*Math.PI*2/3+t*.045;
            for(int i=0;i<steps&&room();i++) {
                double u=i/(double)(steps-1),v=(i+1)/(double)(steps-1);
                double a=phase+u*Math.PI*5,b=phase+v*Math.PI*5;
                double px=x+Math.sin(a)*rx*.74,py=y+.35+u*3.1*open,pz=z+Math.cos(a)*.16;
                double qx=x+Math.sin(b)*rx*.74,qy=y+.35+v*3.1*open,qz=z+Math.cos(b)*.16;
                lightningSegment(px,py,pz,qx,qy,qz,.045,strand==0?.7f:.22f,strand==1?.2f:.6f,1,1);
            }
        }
        ring(x,y+.04,z,.12,.6*open,48,.55f,.08f,.95f);
    }

    private record Scene(String id,double x,double y,double z,int duration,int age) { }
}
