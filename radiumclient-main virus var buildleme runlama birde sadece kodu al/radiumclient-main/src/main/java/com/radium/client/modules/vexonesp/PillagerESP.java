package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.renderer.ShapeMode;
import me.vexon.client.settings.BoolSetting;
import me.vexon.client.settings.ColorSetting;
import me.vexon.client.settings.EnumSetting;
import me.vexon.client.settings.Setting;
import me.vexon.client.settings.SettingGroup;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.render.RenderUtils;
import me.vexon.client.utils.render.color.Color;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PillagerESP extends Module {
    private final SettingGroup sgRender = settings.createGroup("Rendering");

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
            .name("color")
            .description("Color of the ESP box.")
            .defaultValue(new SettingColor(255, 50, 50, 200))
            .build());

    private final Setting<RenderMode> mode = sgRender.add(new EnumSetting.Builder<RenderMode>()
            .name("mode")
            .description("How the ESP should be rendered.")
            .defaultValue(RenderMode.Both)
            .build());

    private final Setting<Boolean> tracers = sgRender.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draw tracers from the player to pillagers holding crossbows.")
            .defaultValue(true)
            .build());

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("Tracer color.")
            .defaultValue(new SettingColor(255, 50, 50, 200))
            .visible(tracers::get)
            .build());

    public PillagerESP() {
        super(Category.VEXON_ESP, "Pillager ESP", "Highlights pillagers holding crossbows.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null)
            return;

        Color espColor = new Color(color.get());
        ShapeMode shapeMode = switch (mode.get()) {
            case Box -> ShapeMode.Sides;
            case Lines -> ShapeMode.Lines;
            case Both -> ShapeMode.Both;
        };

        Vec3d tracerStart = RenderUtils.center != null ? RenderUtils.center
                : mc.gameRenderer.getCamera().getCameraPos();
        Color lineColor = tracers.get() ? new Color(tracerColor.get()) : null;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PillagerEntity pillager))
                continue;
            if (!isHoldingCrossbow(pillager))
                continue;

            double x = MathHelper.lerp(event.tickDelta, pillager.lastRenderX, pillager.getX()) - tracerStart.x;
            double y = MathHelper.lerp(event.tickDelta, pillager.lastRenderY, pillager.getY()) - tracerStart.y;
            double z = MathHelper.lerp(event.tickDelta, pillager.lastRenderZ, pillager.getZ()) - tracerStart.z;

            event.renderer.box(x + pillager.getBoundingBox().minX - pillager.getX(),
                    y + pillager.getBoundingBox().minY - pillager.getY(),
                    z + pillager.getBoundingBox().minZ - pillager.getZ(),
                    x + pillager.getBoundingBox().maxX - pillager.getX(),
                    y + pillager.getBoundingBox().maxY - pillager.getY(),
                    z + pillager.getBoundingBox().maxZ - pillager.getZ(),
                    espColor, espColor, shapeMode, 0);

            if (lineColor != null) {
                double lx = MathHelper.lerp(event.tickDelta, pillager.lastRenderX, pillager.getX());
                double ly = MathHelper.lerp(event.tickDelta, pillager.lastRenderY, pillager.getY());
                double lz = MathHelper.lerp(event.tickDelta, pillager.lastRenderZ, pillager.getZ());
                ly += pillager.getBoundingBox().getLengthY() / 2.0;
                event.renderer.line(tracerStart.x, tracerStart.y, tracerStart.z, lx, ly, lz, lineColor);
            }
        }
    }

    private boolean isHoldingCrossbow(PillagerEntity pillager) {
        return pillager.getStackInHand(Hand.MAIN_HAND).getItem() == Items.CROSSBOW
                || pillager.getStackInHand(Hand.OFF_HAND).getItem() == Items.CROSSBOW;
    }

    public enum RenderMode {
        Lines,
        Box,
        Both
    }
}
