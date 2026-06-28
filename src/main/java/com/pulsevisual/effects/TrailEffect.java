package com.pulsevisual.effects;

import com.pulsevisual.client.PulseVisual;
import com.pulsevisual.config.PulseConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrailEffect {

    private static final int MAX_TRAIL_POINTS = 40;
    private final List<TrailPoint> points = new ArrayList<>();
    private Vec3d lastPos = null;
    private int tickCount = 0;

    public void tick(MinecraftClient client) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.trailEnabled || client.player == null) {
            points.clear();
            lastPos = null;
            return;
        }

        Vec3d pos = client.player.getPos();
        if (lastPos == null || pos.distanceTo(lastPos) > 0.05) {
            points.add(new TrailPoint(pos, tickCount));
            lastPos = pos;
        }

        Iterator<TrailPoint> it = points.iterator();
        while (it.hasNext()) {
            TrailPoint p = it.next();
            if (tickCount - p.spawnTick > MAX_TRAIL_POINTS) {
                it.remove();
            }
        }

        tickCount++;
        if (points.size() > MAX_TRAIL_POINTS) {
            points.remove(0);
        }
    }

    public void render(WorldRenderContext context) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.trailEnabled || points.size() < 2) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = context.camera().getPos();

        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int r = cfg.getTrailRed();
        int g = cfg.getTrailGreen();
        int b = cfg.getTrailBlue();
        float size = cfg.trailSize * 0.05f;

        for (int i = 0; i < points.size(); i++) {
            float progress = (float) i / points.size();
            float alpha = (int) (cfg.trailAlpha * progress * 255);
            TrailPoint p = points.get(i);

            if (cfg.trailMode == PulseConfig.TrailMode.RAINBOW) {
                float hue = (tickCount * 2f + i * 10f) % 360f / 360f;
                int[] rgb = hsvToRgb(hue, 1f, 1f);
                r = rgb[0]; g = rgb[1]; b = rgb[2];
            }

            buffer.vertex(matrix, (float)(p.pos.x), (float)(p.pos.y + 0.9 + size), (float)(p.pos.z))
                  .color(r, g, b, (int)alpha).next();
            buffer.vertex(matrix, (float)(p.pos.x), (float)(p.pos.y + 0.9 - size), (float)(p.pos.z))
                  .color(r, g, b, (int)alpha).next();
        }

        tessellator.draw();

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        matrices.pop();
    }

    private int[] hsvToRgb(float h, float s, float v) {
        int i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0: r=v; g=t; b=p; break;
            case 1: r=q; g=v; b=p; break;
            case 2: r=p; g=v; b=t; break;
            case 3: r=p; g=q; b=v; break;
            case 4: r=t; g=p; b=v; break;
            default: r=v; g=p; b=q;
        }
        return new int[]{(int)(r*255),(int)(g*255),(int)(b*255)};
    }

    private static class TrailPoint {
        Vec3d pos;
        int spawnTick;
        TrailPoint(Vec3d pos, int tick) { this.pos = pos; this.spawnTick = tick; }
    }
}
