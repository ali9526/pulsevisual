package com.pulsevisual.mixin;

import com.pulsevisual.client.PulseVisual;
import com.pulsevisual.config.PulseConfig;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class FogMixin {

    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void onApplyFog(Camera camera, BackgroundRenderer.FogType fogType,
                                    float viewDistance, boolean thickFog, float tickDelta,
                                    CallbackInfo ci) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.fogEnabled) return;

        float density = cfg.fogDensity;
        float dist = cfg.fogDistance * 16f;
        float start = dist * (1f - density);

        com.mojang.blaze3d.systems.RenderSystem.setShaderFogStart(start);
        com.mojang.blaze3d.systems.RenderSystem.setShaderFogEnd(dist);
        com.mojang.blaze3d.systems.RenderSystem.setShaderFogColor(
            cfg.getFogRed(), cfg.getFogGreen(), cfg.getFogBlue(), 1.0f
        );
        com.mojang.blaze3d.systems.RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }
}
