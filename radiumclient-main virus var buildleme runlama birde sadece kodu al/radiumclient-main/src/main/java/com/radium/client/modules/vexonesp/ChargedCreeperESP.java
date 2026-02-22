package me.vexon.client.modules.vexonesp;

import me.vexon.client.client.VexonClient;
import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.ColorSetting;
import me.vexon.client.gui.settings.ModeSetting;
import me.vexon.client.modules.Module;
import me.vexon.client.gui.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class ChargedCreeperESP extends Module {

    private final ColorSetting color = new ColorSetting("Color", new Color(0, 255, 0, 200).getRGB());
    private final ModeSetting mode = new ModeSetting("Mode", "Both", "Box", "Lines", "Both");
    private final BoolSetting tracers = new BoolSetting("Tracers", true);
    private final ColorSetting tracerColor = new ColorSetting("Tracer Color", new Color(0, 255, 0, 200).getRGB());

    public ChargedCreeperESP() {
        super("ChargedCreeperESP", "Highlights charged creepers.", Category.VEXON_ESP);
        this.addSettings(color, mode, tracers, tracerColor);
    }

    @Override
    public void onEnable() {
        WorldRenderEvents.AFTER_ENTITIES.register(this::onRender3D);
        super.onEnable();
    }

    private void onRender3D(WorldRenderContext context) {
        if (!this.isEnabled() || mc.player == null || mc.world == null)
            return;

        Color espColor = new Color(color.getValue());
        Color lineTracerColor = new Color(tracerColor.getValue());

        Vec3d camPos = context.camera().getPos();
        Vec3d tracerStart = RenderUtils.getCameraPos();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof CreeperEntity creeper))
                continue;
            if (!creeper.isCharged())
                continue;

            double tickDelta = context.tickCounter().getTickDelta(true);
            double x = MathHelper.lerp(tickDelta, creeper.lastRenderX, creeper.getX());
            double y = MathHelper.lerp(tickDelta, creeper.lastRenderY, creeper.getY());
            double z = MathHelper.lerp(tickDelta, creeper.lastRenderZ, creeper.getZ());

            Box box = creeper.getBoundingBox().offset(x - creeper.getX(), y - creeper.getY(), z - creeper.getZ());

            context.matrixStack().push();
            context.matrixStack().translate(-camPos.x, -camPos.y, -camPos.z);

            String renderMode = mode.getValue();
            if (renderMode.equals("Box") || renderMode.equals("Both")) {
                RenderUtils.drawBox(context.matrixStack(), box, espColor.getRGB(), false);
            }
            if (renderMode.equals("Lines") || renderMode.equals("Both")) {
                RenderUtils.drawBox(context.matrixStack(), box, espColor.getRGB(), true);
            }

            if (tracers.getValue()) {
                RenderUtils.renderLine(context.matrixStack(), lineTracerColor, tracerStart,
                        new Vec3d(x, y + creeper.getBoundingBox().getLengthY() / 2.0, z));
            }

            context.matrixStack().pop();
        }
    }
}
