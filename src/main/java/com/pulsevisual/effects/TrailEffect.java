package com.pulsevisual.effects;

import com.pulsevisual.client.PulseVisual;
import com.pulsevisual.config.PulseConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrailEffect {
    private static final int MAX = 40;
    private final List<TrailPoint> points = new ArrayList<>();
    private Vec3d lastPos = null;
    private int tick = 0;

    public void tick(MinecraftClient client) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.trailEnabled || client.player == null) { points.clear(); lastPos = null; return; }
        Vec3d pos = client.player.getPos();
        if (lastPos == null || pos.distanceTo(lastPos) > 0.05) {
            points.add(new TrailPoint(pos, tick));
            lastPos = pos;
        }
        points.removeIf(p -> tick - p.spawnTick > MAX);
        if (points.size() > MAX) points.remove(0);
        tick++;
    }

    public void render(WorldRenderContext ctx) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.trailEnabled || points.size() < 2) return;
        MatrixStack ms = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        Matrix4f mat = ms.peek().getPositionMatrix();
        buf.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        int r = cfg.getTrailRed(), g = cfg.getTrailGreen(), b = cfg.getTrailBlue();
        float size = cfg.trailSize * 0.05f;
        for (int i = 0; i < points.size(); i++) {
            float prog = (float) i / points.size();
            int a = (int)(cfg.trailAlpha * prog * 255);
            TrailPoint p = points.get(i);
            if (cfg.trailMode == PulseConfig.TrailMode.RAINBOW) {
                float hue = (tick * 2f + i * 10f) % 360f / 360f;
                int[] rgb = hsvToRgb(hue); r = rgb[0]; g = rgb[1]; b = rgb[2];
            }
            buf.vertex(mat, (float)p.pos.x, (float)(p.pos.y + 0.9 + size), (float)p.pos.z).color(r,g,b,a).next();
            buf.vertex(mat, (float)p.pos.x, (float)(p.pos.y + 0.9 - size), (float)p.pos.z).color(r,g,b,a).next();
        }
        tess.draw();
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        ms.pop();
    }

    private int[] hsvToRgb(float h) {
        int i = (int)(h * 6); float f = h*6-i, p = 1-f, q = 1-(1-f);
        float r,g,b;
        switch(i%6){case 0:r=1;g=q;b=0;break;case 1:r=p;g=1;b=0;break;case 2:r=0;g=1;b=q;break;case 3:r=0;g=p;b=1;break;case 4:r=q;g=0;b=1;break;default:r=1;g=0;b=p;}
        return new int[]{(int)(r*255),(int)(g*255),(int)(b*255)};
    }

    private static class TrailPoint {
        Vec3d pos; int spawnTick;
        TrailPoint(Vec3d pos, int t) { this.pos=pos; this.spawnTick=t; }
    }
}
