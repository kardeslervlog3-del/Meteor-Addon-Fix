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
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DrownedTridentESP extends Module {
    private final SettingGroup sgRender = settings.createGroup("Rendering");

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
            .name("color")
            .description("Color of the ESP box.")
            .defaultValue(new SettingColor(50, 255, 100, 200))
            .build());

    private final Setting<RenderMode> mode = sgRender.add(new EnumSetting.Builder<RenderMode>()
            .name("mode")
            .description("How the ESP should be rendered.")
            .defaultValue(RenderMode.Both)
            .build());

    private final Setting<Boolean> tracers = sgRender.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draw tracers from the player to drowned holding tridents.")
            .defaultValue(true)
            .build());

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("Tracer color.")
            .defaultValue(new SettingColor(255, 50, 50, 200))
            .visible(tracers::get)
            .build());

    public DrownedTridentESP() {
        super(Category.VEXON_ESP, "Drowned Trident ESP", "Highlights drowned mobs holding tridents.");
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
            if (!(entity instanceof DrownedEntity drowned))
                continue;
            if (!isHoldingTrident(drowned))
                continue;

            double x = MathHelper.lerp(event.tickDelta, drowned.lastRenderX, drowned.getX()) - tracerStart.x;
            double y = MathHelper.lerp(event.tickDelta, drowned.lastRenderY, drowned.getY()) - tracerStart.y;
            double z = MathHelper.lerp(event.tickDelta, drowned.lastRenderZ, drowned.getZ()) - tracerStart.z;

            event.renderer.box(x + drowned.getBoundingBox().minX - drowned.getX(),
                    y + drowned.getBoundingBox().minY - drowned.getY(),
                    z + drowned.getBoundingBox().minZ - drowned.getZ(),
                    x + drowned.getBoundingBox().maxX - drowned.getX(),
                    y + drowned.getBoundingBox().maxY - drowned.getY(),
                    z + drowned.getBoundingBox().maxZ - drowned.getZ(),
                    espColor, espColor, shapeMode, 0);

            if (lineColor != null) {
                x = MathHelper.lerp(event.tickDelta, drowned.lastRenderX, drowned.getX());
                y = MathHelper.lerp(event.tickDelta, drowned.lastRenderY, drowned.getY());
                z = MathHelper.lerp(event.tickDelta, drowned.lastRenderZ, drowned.getZ());
                y += drowned.getBoundingBox().getLengthY() / 2.0;
                event.renderer.line(tracerStart.x, tracerStart.y, tracerStart.z, x, y, z, lineColor);
            }
        }
    }

    private boolean isHoldingTrident(DrownedEntity drowned) {
        return drowned.getStackInHand(Hand.MAIN_HAND).getItem() == Items.TRIDENT
                || drowned.getStackInHand(Hand.OFF_HAND).getItem() == Items.TRIDENT;
    }

    public enum RenderMode {
        Lines,
        Box,
        Both
    }
}
