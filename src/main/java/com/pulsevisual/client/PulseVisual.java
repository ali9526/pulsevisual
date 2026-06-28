package com.pulsevisual.client;

import com.pulsevisual.config.PulseConfig;
import com.pulsevisual.effects.TrailEffect;
import com.pulsevisual.effects.JumpCircleEffect;
import com.pulsevisual.effects.HitEffect;
import com.pulsevisual.gui.PulseScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PulseVisual implements ClientModInitializer {

    public static final String MOD_ID = "pulsevisual";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static PulseConfig config;
    public static TrailEffect trailEffect;
    public static JumpCircleEffect jumpCircleEffect;
    public static HitEffect hitEffect;

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("PulseVisual loaded!");

        config = new PulseConfig();
        trailEffect = new TrailEffect();
        jumpCircleEffect = new JumpCircleEffect();
        hitEffect = new HitEffect();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.pulsevisual.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.pulsevisual"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PulseScreen());
                }
            }
            if (client.player != null) {
                trailEffect.tick(client);
                jumpCircleEffect.tick(client);
                hitEffect.tick(client);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null) return;
            trailEffect.render(context);
            jumpCircleEffect.render(context);
            hitEffect.render(context);
        });
    }
}
