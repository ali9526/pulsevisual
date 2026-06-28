package com.pulsevisual.effects;

import com.pulsevisual.client.PulseVisual;
import com.pulsevisual.config.PulseConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HitEffect {

    private final List<HitParticle> particles = new ArrayList<>();

    public void tick(MinecraftClient client) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.hitColorEnabled || client.world == null || client.player == null) {
            particles.clear();
            return;
        }

        Iterator<HitParticle> it = particles.iterator();
        while (it.hasNext()) {
            HitParticle p = it.next();
            p.tick();
            if (p.isDead()) it.remove();
        }
    }

    // Called from mixin when player hits entity
    public void onHit(Vec3d pos) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.hitColorEnabled) return;
        for (int i = 0; i < 8; i++) {
            double vx = (Math.random() - 0.5) * 0.3;
            double vy = Math.random() * 0.2 + 0.05;
            double vz = (Math.random() - 0.5) * 0.3;
            particles.add(new HitParticle(pos, new Vec3d(vx, vy, vz)));
        }
    }

    public void render(WorldRenderContext context) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.hitColorEnabled || particles.isEmpty()) return;

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

        int r = cfg.getHitRed();
        int g = cfg.getHitGreen();
        int b = cfg.getHitBlue();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (HitParticle p : particles) {
            float alpha = (1f - p.getProgress()) * cfg.hitColorAlpha;
            int a = (int)(alpha * 255);
            float s = 0.08f;
            float x = (float) p.pos.x;
            float y = (float) p.pos.y;
            float z = (float) p.pos.z;
            buffer.vertex(matrix, x-s, y-s, z).color(r,g,b,a).next();
            buffer.vertex(matrix, x+s, y-s, z).color(r,g,b,a).next();
            buffer.vertex(matrix, x+s, y+s, z).color(r,g,b,a).next();
            buffer.vertex(matrix, x-s, y+s, z).color(r,g,b,a).next();
        }
        tessellator.draw();

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        matrices.pop();
    }

    private static class HitParticle {
        Vec3d pos;
        Vec3d vel;
        int age = 0;
        static final int LIFETIME = 15;

        HitParticle(Vec3d pos, Vec3d vel) { this.pos = pos; this.vel = vel; }
        void tick() {
            pos = pos.add(vel);
            vel = new Vec3d(vel.x * 0.85, vel.y - 0.015, vel.z * 0.85);
            age++;
        }
        boolean isDead() { return age >= LIFETIME; }
        float getProgress() { return (float) age / LIFETIME; }
    }
}
