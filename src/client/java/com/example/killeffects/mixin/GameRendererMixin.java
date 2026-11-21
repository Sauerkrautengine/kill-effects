package com.example.killeffects.mixin;

import com.example.killeffects.effect.VisualEffectRenderer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "close", at = @At("RETURN"))
    private void killeffects$releaseMeshBuffer(CallbackInfo ci) {
        VisualEffectRenderer.close();
    }
}
