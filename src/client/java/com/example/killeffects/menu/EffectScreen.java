package com.example.killeffects.menu;

import com.example.killeffects.config.EffectConfig;
import com.example.killeffects.effect.EffectManager;
import com.example.killeffects.effect.KillEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EffectScreen extends Screen {
    private String category="All Effects";
    private int page;
    public EffectScreen() { super(Component.literal("Kill Effects")); }

    @Override protected void init() {
        clearWidgets();
        String[] cats={"All Effects","Magical","Funny","Explosive","Classic"};
        int left=(width-500)/2;
        for(int i=0;i<cats.length;i++) { final String c=cats[i]; addRenderableWidget(Button.builder(Component.literal(c),b->{category=c;page=0;init();}).bounds(left+i*101,34,98,20).build()); }
        var effects=EffectManager.EFFECTS.stream().filter(e->category.equals("All Effects")||e.category().equals(category)).toList();
        int rows=4, start=page*rows*2;
        for(int slot=0;slot<rows*2&&start+slot<effects.size();slot++) {
            KillEffect e=effects.get(start+slot); int col=slot%2,row=slot/2; int bx=left+col*254, by=72+row*52;
            String mark=EffectConfig.selected.equals(e.id())?"  ✓":"";
            addRenderableWidget(Button.builder(Component.literal(icon(e.id())+"  "+e.name()+mark),b->{EffectConfig.selected=e.id();EffectConfig.save();init();}).bounds(bx,by,246,22).build());
            addRenderableWidget(Button.builder(Component.literal(EffectConfig.isEffectEnabled(e.id())?"Disable":"Enable"),b->{EffectConfig.toggleEffect(e.id());init();}).bounds(bx+82,by+25,78,19).build());
            addRenderableWidget(Button.builder(Component.literal("Preview"),b->EffectManager.preview(Minecraft.getInstance(),e)).bounds(bx+166,by+25,80,19).build());
        }
        if(start+rows*2<effects.size()) addRenderableWidget(Button.builder(Component.literal("Next ›"),b->{page++;init();}).bounds(left+408,286,90,20).build());
        if(page>0) addRenderableWidget(Button.builder(Component.literal("‹ Previous"),b->{page--;init();}).bounds(left+304,286,98,20).build());
        addRenderableWidget(Button.builder(Component.literal(EffectConfig.enabled?"Effects: ON":"Effects: OFF"),b->{EffectConfig.enabled=!EffectConfig.enabled;EffectConfig.save();init();}).bounds(left,316,116,20).build());
        addRenderableWidget(Button.builder(Component.literal("Particle limit: "+EffectConfig.particleLimit),b->{EffectConfig.particleLimit=EffectConfig.particleLimit>=600?60:EffectConfig.particleLimit+60;EffectConfig.save();init();}).bounds(left+122,316,142,20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop preview"),b->EffectManager.stop()).bounds(left+270,316,100,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left+376,316,122,20).build());
    }
    private String icon(String id){return switch(id){case "soul"->"✧";case "meteor"->"☄";case "void"->"◉";case "anvil"->"▣";case "cards"->"▤";case "thunder"->"ϟ";case "firework"->"✹";case "arcane"->"◇";case "tornado"->"〰";default->"★";};}
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta){
        super.extractRenderState(graphics,mouseX,mouseY,delta);
        int left=(width-500)/2;
        graphics.text(font,"KILL EFFECTS",left,12,0xFFE9F4FF,true);
        graphics.text(font,"Select a cosmetic effect · click Preview to watch it",left,55,0xFF9FB1C8,true);
        var selected=EffectManager.find(EffectConfig.selected);
        graphics.text(font,selected.name()+" — "+selected.description(),left,294,0xFFB7C8DD,true);
    }
    @Override public boolean isPauseScreen(){return false;}
}
