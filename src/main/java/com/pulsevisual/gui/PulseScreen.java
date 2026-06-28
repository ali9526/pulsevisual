package com.pulsevisual.gui;

import com.pulsevisual.client.PulseVisual;
import com.pulsevisual.config.PulseConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class PulseScreen extends Screen {

    // Tab state
    private int currentTab = 0; // 0=Visual, 1=World
    private int selectedItem = -1;

    // Scroll / layout
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 40;
    private static final int PANEL_W = 140;
    private static final int PANEL_H = 200;
    private static final int SETTINGS_X = 300;
    private static final int SETTINGS_Y = 40;
    private static final int SETTINGS_W = 200;
    private static final int ITEM_H = 18;

    // Color palette
    private static final int[] PALETTE = {
        0xA78BFA, 0x38BDF8, 0xF472B6, 0x34D399,
        0xFB923C, 0xF87171, 0xFACC15, 0xFFFFFF
    };

    // Dragging color sliders
    private boolean draggingR = false, draggingG = false, draggingB = false;
    private boolean draggingFog = false, draggingFogDist = false;

    public PulseScreen() {
        super(Text.literal("PulseVisual"));
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dark semi-transparent background
        ctx.fill(0, 0, this.width, this.height, 0x88000000);

        // Tab bar
        drawTabBar(ctx, mouseX, mouseY);

        if (currentTab == 0) drawVisualTab(ctx, mouseX, mouseY);
        else drawWorldTab(ctx, mouseX, mouseY);

        drawSettingsPanel(ctx, mouseX, mouseY);
    }

    private void drawTabBar(DrawContext ctx, int mx, int my) {
        String[] tabs = {"Visual", "World"};
        int tx = PANEL_X;
        for (int i = 0; i < tabs.length; i++) {
            int tw = 60, th = 16;
            int ty = PANEL_Y - 20;
            boolean hover = mx >= tx && mx < tx+tw && my >= ty && my < ty+th;
            boolean active = currentTab == i;
            int bg = active ? 0xAA7C3AED : (hover ? 0x55FFFFFF : 0x33FFFFFF);
            int border = active ? 0xAA7C3AED : 0x44FFFFFF;
            ctx.fill(tx, ty, tx+tw, ty+th, bg);
            drawBorder(ctx, tx, ty, tw, th, border);
            ctx.drawCenteredTextWithShadow(this.textRenderer, tabs[i], tx + tw/2, ty + 4, active ? 0xD8B4FE : 0xCCFFFFFF);
            tx += tw + 4;
        }
    }

    private void drawVisualTab(DrawContext ctx, int mx, int my) {
        // Panel 1: Visual Effects
        drawPanel(ctx, PANEL_X, PANEL_Y, PANEL_W, PANEL_H, "Visual Effects");
        PulseConfig cfg = PulseVisual.config;
        String[] items = {"Trail", "Jump Circle", "Hit Color"};
        boolean[] enabled = {cfg.trailEnabled, cfg.jumpCircleEnabled, cfg.hitColorEnabled};
        for (int i = 0; i < items.length; i++) {
            drawItem(ctx, PANEL_X, PANEL_Y + 14 + i * ITEM_H, PANEL_W, items[i], enabled[i], selectedItem == i, mx, my);
        }
    }

    private void drawWorldTab(DrawContext ctx, int mx, int my) {
        drawPanel(ctx, PANEL_X, PANEL_Y, PANEL_W, PANEL_H, "World");
        PulseConfig cfg = PulseVisual.config;
        String[] items = {"Sky Color", "Fog"};
        boolean[] enabled = {cfg.skyColorEnabled, cfg.fogEnabled};
        int base = 10;
        for (int i = 0; i < items.length; i++) {
            drawItem(ctx, PANEL_X, PANEL_Y + 14 + i * ITEM_H, PANEL_W, items[i], enabled[i], selectedItem == base + i, mx, my);
        }
    }

    private void drawSettingsPanel(DrawContext ctx, int mx, int my) {
        PulseConfig cfg = PulseVisual.config;
        int x = SETTINGS_X, y = SETTINGS_Y, w = SETTINGS_W;

        if (selectedItem == 0) drawEffectSettings(ctx, x, y, w, mx, my, "Trail",
            cfg.trailEnabled, cfg.trailColor, cfg.trailAlpha, cfg.trailSize);
        else if (selectedItem == 1) drawEffectSettings(ctx, x, y, w, mx, my, "Jump Circle",
            cfg.jumpCircleEnabled, cfg.jumpCircleColor, cfg.jumpCircleAlpha, (int)(cfg.jumpCircleSize*10));
        else if (selectedItem == 2) drawEffectSettings(ctx, x, y, w, mx, my, "Hit Color",
            cfg.hitColorEnabled, cfg.hitColor, cfg.hitColorAlpha, 5);
        else if (selectedItem == 10) drawSkySettings(ctx, x, y, w, mx, my);
        else if (selectedItem == 11) drawFogSettings(ctx, x, y, w, mx, my);
    }

    private void drawEffectSettings(DrawContext ctx, int x, int y, int w, int mx, int my,
                                     String name, boolean enabled, int color, float alpha, int size) {
        int h = 220;
        ctx.fill(x, y, x+w, y+h, 0x99111122);
        drawBorder(ctx, x, y, w, h, 0x55FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, name + " — settings", x+8, y+6, 0xFFD8B4FE);

        // Toggle
        int ty = y + 22;
        ctx.drawTextWithShadow(this.textRenderer, "Enabled", x+8, ty+2, 0xAAFFFFFF);
        drawToggle(ctx, x+w-36, ty, enabled);

        // Color palette
        ty += 22;
        ctx.drawTextWithShadow(this.textRenderer, "Color", x+8, ty, 0xAAFFFFFF);
        ty += 12;
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = x + 8 + i * 22;
            boolean sel = PALETTE[i] == color;
            drawColorSwatch(ctx, cx, ty, PALETTE[i], sel);
        }

        // Alpha slider
        ty += 24;
        ctx.drawTextWithShadow(this.textRenderer, "Alpha", x+8, ty, 0xAAFFFFFF);
        int pct = (int)(alpha * 100);
        drawSlider(ctx, x+8, ty+10, w-20, pct, 0, 100, "%");

        // Size slider
        ty += 32;
        ctx.drawTextWithShadow(this.textRenderer, "Size", x+8, ty, 0xAAFFFFFF);
        drawSlider(ctx, x+8, ty+10, w-20, size, 1, 10, "");

        // Preview dot
        ty += 40;
        ctx.drawTextWithShadow(this.textRenderer, "Preview:", x+8, ty, 0x88FFFFFF);
        int pr = (color >> 16) & 0xFF;
        int pg = (color >> 8)  & 0xFF;
        int pb =  color        & 0xFF;
        ctx.fill(x+60, ty-1, x+70, ty+9, 0xFF000000 | color);
    }

    private void drawSkySettings(DrawContext ctx, int x, int y, int w, int mx, int my) {
        PulseConfig cfg = PulseVisual.config;
        int h = 200;
        ctx.fill(x, y, x+w, y+h, 0x99111122);
        drawBorder(ctx, x, y, w, h, 0x55FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "Sky Color — settings", x+8, y+6, 0xFFD8B4FE);

        int ty = y + 22;
        ctx.drawTextWithShadow(this.textRenderer, "Enabled", x+8, ty+2, 0xAAFFFFFF);
        drawToggle(ctx, x+w-36, ty, cfg.skyColorEnabled);

        ty += 22;
        ctx.drawTextWithShadow(this.textRenderer, "Sky color", x+8, ty, 0xAAFFFFFF);
        ty += 12;
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = x + 8 + i * 22;
            boolean sel = PALETTE[i] == cfg.skyColor;
            drawColorSwatch(ctx, cx, ty, PALETTE[i], sel);
        }

        // Sky preview gradient
        ty += 28;
        int skyC = cfg.skyColor;
        int skyLight = blendColor(skyC, 0xFFFFFF, 0.5f);
        for (int row = 0; row < 20; row++) {
            float t = (float) row / 20;
            int c = blendColor(skyC, skyLight, t);
            ctx.fill(x+8, ty+row, x+w-8, ty+row+1, 0xFF000000 | c);
        }
        ctx.drawTextWithShadow(this.textRenderer, "Preview", x+8, ty-10, 0x88FFFFFF);
    }

    private void drawFogSettings(DrawContext ctx, int x, int y, int w, int mx, int my) {
        PulseConfig cfg = PulseVisual.config;
        int h = 180;
        ctx.fill(x, y, x+w, y+h, 0x99111122);
        drawBorder(ctx, x, y, w, h, 0x55FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "Fog — settings", x+8, y+6, 0xFFD8B4FE);

        int ty = y + 22;
        ctx.drawTextWithShadow(this.textRenderer, "Enabled", x+8, ty+2, 0xAAFFFFFF);
        drawToggle(ctx, x+w-36, ty, cfg.fogEnabled);

        ty += 22;
        ctx.drawTextWithShadow(this.textRenderer, "Density", x+8, ty, 0xAAFFFFFF);
        drawSlider(ctx, x+8, ty+10, w-20, (int)(cfg.fogDensity*100), 0, 100, "%");

        ty += 32;
        ctx.drawTextWithShadow(this.textRenderer, "Distance", x+8, ty, 0xAAFFFFFF);
        drawSlider(ctx, x+8, ty+10, w-20, cfg.fogDistance, 2, 32, " chunks");

        ty += 32;
        ctx.drawTextWithShadow(this.textRenderer, "Fog color", x+8, ty, 0xAAFFFFFF);
        ty += 12;
        for (int i = 0; i < PALETTE.length; i++) {
            int cx = x + 8 + i * 22;
            boolean sel = PALETTE[i] == cfg.fogColor;
            drawColorSwatch(ctx, cx, ty, PALETTE[i], sel);
        }
    }

    // ---- helpers ----

    private void drawPanel(DrawContext ctx, int x, int y, int w, int h, String title) {
        ctx.fill(x, y, x+w, y+h, 0x99111122);
        drawBorder(ctx, x, y, w, h, 0x44FFFFFF);
        ctx.fill(x, y, x+w, y+13, 0x44FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, title, x+6, y+3, 0xFFD8B4FE);
    }

    private void drawItem(DrawContext ctx, int px, int py, int pw, String name, boolean enabled, boolean selected, int mx, int my) {
        boolean hover = mx >= px && mx < px+pw && my >= py && my < py+ITEM_H;
        int bg = selected ? 0x44A78BFA : (hover ? 0x22FFFFFF : 0x00000000);
        ctx.fill(px+2, py, px+pw-2, py+ITEM_H-1, bg);
        int textColor = selected ? 0xFFD8B4FE : (hover ? 0xFFFFFFFF : 0xAAFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, name, px+8, py+5, textColor);
        // enabled dot
        int dotColor = enabled ? 0xFF7C3AED : 0x55FFFFFF;
        ctx.fill(px+pw-12, py+6, px+pw-6, py+12, dotColor);
    }

    private void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        int bg = on ? 0xAA7C3AED : 0x33FFFFFF;
        ctx.fill(x, y, x+28, y+14, bg);
        drawBorder(ctx, x, y, 28, 14, 0x44FFFFFF);
        int knobX = on ? x+16 : x+2;
        ctx.fill(knobX, y+2, knobX+10, y+12, 0xFFFFFFFF);
    }

    private void drawColorSwatch(DrawContext ctx, int x, int y, int color, boolean selected) {
        ctx.fill(x, y, x+16, y+16, 0xFF000000 | color);
        if (selected) {
            drawBorder(ctx, x-1, y-1, 18, 18, 0xFFFFFFFF);
        }
    }

    private void drawSlider(DrawContext ctx, int x, int y, int w, int val, int min, int max, String suffix) {
        ctx.fill(x, y+5, x+w, y+9, 0x33FFFFFF);
        float t = (float)(val - min) / (max - min);
        int fillW = (int)(w * t);
        ctx.fill(x, y+5, x+fillW, y+9, 0xAA7C3AED);
        ctx.fill(x+fillW-4, y+2, x+fillW+4, y+12, 0xFFFFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, val + suffix, x+w+4, y+2, 0xFFA78BFA);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x+w, y+1, color);
        ctx.fill(x, y+h-1, x+w, y+h, color);
        ctx.fill(x, y, x+1, y+h, color);
        ctx.fill(x+w-1, y, x+w, y+h, color);
    }

    private int blendColor(int a, int b, float t) {
        int ar=(a>>16)&0xFF, ag=(a>>8)&0xFF, ab=a&0xFF;
        int br=(b>>16)&0xFF, bg=(b>>8)&0xFF, bb=b&0xFF;
        int r=(int)(ar+(br-ar)*t), g=(int)(ag+(bg-ag)*t), bv=(int)(ab+(bb-ab)*t);
        return (r<<16)|(g<<8)|bv;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int imx = (int)mx, imy = (int)my;

        // Tab clicks
        int tx = PANEL_X, ty2 = PANEL_Y - 20;
        if (imy >= ty2 && imy < ty2+16) {
            if (imx >= tx && imx < tx+60) { currentTab = 0; selectedItem = -1; }
            else if (imx >= tx+64 && imx < tx+124) { currentTab = 1; selectedItem = -1; }
            return true;
        }

        // Item clicks
        if (currentTab == 0) {
            String[] names = {"Trail","Jump Circle","Hit Color"};
            for (int i = 0; i < names.length; i++) {
                int iy = PANEL_Y + 14 + i * ITEM_H;
                if (imx >= PANEL_X && imx < PANEL_X + PANEL_W && imy >= iy && imy < iy + ITEM_H) {
                    if (selectedItem == i) selectedItem = -1;
                    else selectedItem = i;
                    return true;
                }
            }
        } else {
            String[] names = {"Sky Color","Fog"};
            for (int i = 0; i < names.length; i++) {
                int iy = PANEL_Y + 14 + i * ITEM_H;
                if (imx >= PANEL_X && imx < PANEL_X + PANEL_W && imy >= iy && imy < iy + ITEM_H) {
                    selectedItem = 10 + i;
                    return true;
                }
            }
        }

        // Settings panel clicks
        handleSettingsClick(imx, imy);
        return super.mouseClicked(mx, my, button);
    }

    private void handleSettingsClick(int mx, int my) {
        PulseConfig cfg = PulseVisual.config;
        int x = SETTINGS_X, y = SETTINGS_Y, w = SETTINGS_W;

        // Toggle click
        int toggleX = x + w - 36, toggleY = y + 22;
        if (mx >= toggleX && mx < toggleX+28 && my >= toggleY && my < toggleY+14) {
            if (selectedItem == 0) cfg.trailEnabled = !cfg.trailEnabled;
            else if (selectedItem == 1) cfg.jumpCircleEnabled = !cfg.jumpCircleEnabled;
            else if (selectedItem == 2) cfg.hitColorEnabled = !cfg.hitColorEnabled;
            else if (selectedItem == 10) cfg.skyColorEnabled = !cfg.skyColorEnabled;
            else if (selectedItem == 11) cfg.fogEnabled = !cfg.fogEnabled;
            return;
        }

        // Color palette click
        int paletteY = y + 22 + 22 + 12;
        if (my >= paletteY && my < paletteY + 16) {
            for (int i = 0; i < PALETTE.length; i++) {
                int cx = x + 8 + i * 22;
                if (mx >= cx && mx < cx + 16) {
                    if (selectedItem == 0) cfg.trailColor = PALETTE[i];
                    else if (selectedItem == 1) cfg.jumpCircleColor = PALETTE[i];
                    else if (selectedItem == 2) cfg.hitColor = PALETTE[i];
                    else if (selectedItem == 10) cfg.skyColor = PALETTE[i];
                    else if (selectedItem == 11) cfg.fogColor = PALETTE[i];
                }
            }
        }
    }
}
