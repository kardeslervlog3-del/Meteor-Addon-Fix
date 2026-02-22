package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.settings.BoolSetting;
import me.vexon.client.settings.ColorSetting;
import me.vexon.client.settings.EnumSetting;
import me.vexon.client.settings.Setting;
import me.vexon.client.settings.SettingGroup;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.render.color.Color;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TraderLlamaEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

public class WanderingLamaESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> showTracers = sgRender.add(new BoolSetting.Builder()
            .name("show-tracers")
            .description("Draw tracer lines to wandering trader llamas.")
            .defaultValue(true)
            .build());

    private final Setting<SettingColor> tracerColor = sgRender
            .add(new ColorSetting.Builder()
                    .name("tracer-color")
                    .description("Color of the tracer lines.")
                    .defaultValue(new SettingColor(255, 165, 0, 127))
                    .visible(showTracers::get)
                    .build());

    private final Setting<Boolean> enableDisconnect = sgGeneral.add(new BoolSetting.Builder()
            .name("disconnect")
            .description("Automatically disconnect when wandering trader llamas are detected.")
            .defaultValue(false)
            .build());

    private final Setting<Mode> notificationMode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("notification-mode")
            .description("How to notify when wandering trader llamas are detected.")
            .defaultValue(Mode.Both)
            .build());

    private final Setting<Boolean> toggleOnFind = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-when-found")
            .description("Automatically toggles the module when a wandering trader llama is detected.")
            .defaultValue(false)
            .build());

    private final Set<Integer> detectedLlamas = new HashSet<>();

    public WanderingLamaESP() {
        super(Category.VEXON_ESP, "Wandering Lama ESP", "Detects wandering trader llamas in the world.");
    }

    @Override
    public void onActivate() {
        detectedLlamas.clear();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null)
            return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();
        Color lineCol = new Color(tracerColor.get());

        boolean foundNew = false;
        Set<Integer> currentLlamas = new HashSet<>();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof TraderLlamaEntity llama))
                continue;

            currentLlamas.add(llama.getId());
            if (!detectedLlamas.contains(llama.getId())) {
                foundNew = true;
                detectedLlamas.add(llama.getId());
            }

            if (showTracers.get()) {
                double x = MathHelper.lerp(event.tickDelta, llama.lastRenderX, llama.getX());
                double y = MathHelper.lerp(event.tickDelta, llama.lastRenderY, llama.getY());
                double z = MathHelper.lerp(event.tickDelta, llama.lastRenderZ, llama.getZ());
                y += llama.getBoundingBox().getLengthY() / 2.0;

                event.renderer.line(cameraPos.x, cameraPos.y, cameraPos.z, x, y, z, lineCol);
            }
        }

        if (foundNew) {
            handleLlamaDetection();
        }

        detectedLlamas.retainAll(currentLlamas);
    }

    private void handleLlamaDetection() {
        String msg = "Wandering Trader Llama detected!";

        if (notificationMode.get() == Mode.Chat || notificationMode.get() == Mode.Both) {
            info(msg);
        }

        if (enableDisconnect.get()) {
            mc.getNetworkHandler().getConnection().disconnect(Text.literal(msg));
        }

        if (toggleOnFind.get())
            toggle();
    }

    public enum Mode {
        Chat,
        Toast,
        Both
    }
}
