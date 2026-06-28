package com.pulsevisual.config;

import net.minecraft.util.math.ColorHelper;

public class PulseConfig {

    // Trail
    public boolean trailEnabled = false;
    public int trailColor = 0xA78BFA;
    public float trailAlpha = 0.8f;
    public int trailSize = 3;
    public TrailMode trailMode = TrailMode.PARTICLES;

    // Jump Circle
    public boolean jumpCircleEnabled = false;
    public int jumpCircleColor = 0x38BDF8;
    public float jumpCircleAlpha = 0.7f;
    public float jumpCircleSize = 1.0f;

    // Hit Color
    public boolean hitColorEnabled = false;
    public int hitColor = 0xF472B6;
    public float hitColorAlpha = 0.6f;

    // Sky
    public boolean skyColorEnabled = false;
    public int skyColor = 0x60A5FA;

    // Fog
    public boolean fogEnabled = false;
    public float fogDensity = 0.4f;
    public int fogDistance = 16;
    public int fogColor = 0x94A3B8;

    public enum TrailMode {
        PARTICLES, SOLID, RAINBOW
    }

    public int getTrailRed()   { return (trailColor >> 16) & 0xFF; }
    public int getTrailGreen() { return (trailColor >> 8)  & 0xFF; }
    public int getTrailBlue()  { return  trailColor        & 0xFF; }

    public int getCircleRed()   { return (jumpCircleColor >> 16) & 0xFF; }
    public int getCircleGreen() { return (jumpCircleColor >> 8)  & 0xFF; }
    public int getCircleBlue()  { return  jumpCircleColor        & 0xFF; }

    public int getHitRed()   { return (hitColor >> 16) & 0xFF; }
    public int getHitGreen() { return (hitColor >> 8)  & 0xFF; }
    public int getHitBlue()  { return  hitColor        & 0xFF; }

    public float getSkyRed()   { return ((skyColor >> 16) & 0xFF) / 255f; }
    public float getSkyGreen() { return ((skyColor >> 8)  & 0xFF) / 255f; }
    public float getSkyBlue()  { return ( skyColor        & 0xFF) / 255f; }

    public float getFogRed()   { return ((fogColor >> 16) & 0xFF) / 255f; }
    public float getFogGreen() { return ((fogColor >> 8)  & 0xFF) / 255f; }
    public float getFogBlue()  { return ( fogColor        & 0xFF) / 255f; }
}
