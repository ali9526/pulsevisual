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

public class JumpCircleEffect {

    private final List<CircleInstance> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    public void tick(MinecraftClient client) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.jumpCircleEnabled || client.player == null) {
            circles.clear();
            return;
        }

        boolean onGround = client.player.isOnGround();
        if (!onGround && wasOnGround) {
            // Player jumped — spawn circle
            circles.add(new CircleInstance(client.player.getPos()));
        }
        wasOnGround = onGround;

        Iterator<CircleInstance> it = circles.iterator();
        while (it.hasNext()) {
            CircleInstance c = it.next();
            c.tick();
            if (c.isDead()) it.remove();
        }
    }

    public void render(WorldRenderContext context) {
        PulseConfig cfg = PulseVisual.config;
        if (!cfg.jumpCircleEnabled || circles.isEmpty()) return;

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

        for (CircleInstance c : circles) {
            float progress = c.getProgress();
            float radius = progress * 2.0f * cfg.jumpCircleSize;
            float alpha = (1f - progress) * cfg.jumpCircleAlpha;

            int r = cfg.getCircleRed();
            int g = cfg.getCircleGreen();
            int b = cfg.getCircleBlue();
            int a = (int)(alpha * 255);

            int segments = 64;
            buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = 2 * Math.PI * i / segments;
                float x = (float)(c.pos.x + Math.cos(angle) * radius);
                float z = (float)(c.pos.z + Math.sin(angle) * radius);
                buffer.vertex(matrix, x, (float)c.pos.y + 0.05f, z)
                      .color(r, g, b, a).next();
            }
            tessellator.draw();
        }

        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        matrices.pop();
    }

    private static class CircleInstance {
        Vec3d pos;
        int age = 0;
        static final int LIFETIME = 20;

        CircleInstance(Vec3d pos) { this.pos = new Vec3d(pos.x, pos.y, pos.z); }
        void tick() { age++; }
        boolean isDead() { return age >= LIFETIME; }
        float getProgress() { return (float) age / LIFETIME; }
    }
}
