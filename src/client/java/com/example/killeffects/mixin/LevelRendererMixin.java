package com.example.killeffects.mixin;

import com.example.killeffects.effect.VisualEffectRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @WrapOperation(
            method = "executeClassicTransparency",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/renderpearl/api/commands/RenderPass;Lcom/mojang/renderpearl/api/textures/GpuSampler;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Z)V")
    )
    private void killeffects$drawMeshes(ChunkSectionsToRender sections, ChunkSectionLayerGroup group,
                                        RenderPass pass, GpuSampler sampler, GpuTextureView atlas,
                                        boolean wireframe, Operation<Void> original) {
        original.call(sections, group, pass, sampler, atlas, wireframe);
        VisualEffectRenderer.draw(pass);
    }
}
