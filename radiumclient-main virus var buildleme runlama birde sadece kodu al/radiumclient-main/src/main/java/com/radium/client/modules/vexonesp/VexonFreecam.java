package me.vexon.client.modules.vexonesp;

import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.NumberSetting;
import me.vexon.client.modules.Module;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class VexonFreecam extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.0, 5.0, 0.1);
    public final BoolSetting lockedInteraction = new BoolSetting("Locked Interaction", true);

    public final Vector3d pos = new Vector3d();
    public final Vector3d prevPos = new Vector3d();

    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private boolean forward, backward, left, right, up, down;

    public VexonFreecam() {
        super("VexonFreecam", "Free-roam camera.", Category.VEXON_ESP);
        this.addSettings(speed, lockedInteraction);
    }

    @Override
    public void onEnable() {
        if (mc.player == null)
            return;

        Vec3d eyePos = mc.gameRenderer.getCamera().getCameraPos();
        pos.set(eyePos.x, eyePos.y, eyePos.z);
        prevPos.set((Vector3dc) pos);

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        lastYaw = yaw;
        lastPitch = pitch;

        unpress();
        super.onEnable();
    }

    private void unpress() {
        forward = backward = left = right = up = down = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null)
            return;

        prevPos.set((Vector3dc) pos);
        lastYaw = yaw;
        lastPitch = pitch;

        updateInput();

        Vec3d fwd = Vec3d.fromPolar(0.0f, yaw);
        Vec3d side = Vec3d.fromPolar(0.0f, yaw + 90.0f);

        double s = speed.getValue() * (mc.options.sprintKey.isPressed() ? 2.0 : 1.0) * 0.2;
        double x = 0;
        double y = 0;
        double z = 0;

        if (forward) {
            x += fwd.x * s;
            z += fwd.z * s;
        }
        if (backward) {
            x -= fwd.x * s;
            z -= fwd.z * s;
        }
        if (right) {
            x += side.x * s;
            z += side.z * s;
        }
        if (left) {
            x -= side.x * s;
            z -= side.z * s;
        }

        if (up)
            y += s;
        if (down)
            y -= s;

        pos.add(x, y, z);
    }

    private void updateInput() {
        if (mc.currentScreen != null) {
            unpress();
            return;
        }
        forward = mc.options.forwardKey.isPressed();
        backward = mc.options.backKey.isPressed();
        left = mc.options.leftKey.isPressed();
        right = mc.options.rightKey.isPressed();
        up = mc.options.jumpKey.isPressed();
        down = mc.options.sneakKey.isPressed();
    }

    public void changeLookDirection(double dx, double dy) {
        double s = mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
        double multiplier = s * s * s * 8.0;

        yaw += (float) (dx * multiplier * 0.15);
        pitch += (float) (dy * multiplier * 0.15);
        pitch = MathHelper.clamp(pitch, -90.0f, 90.0f);
    }

    public double getX(float t) {
        return MathHelper.lerp(t, (float) prevPos.x, (float) pos.x);
    }

    public double getY(float t) {
        return MathHelper.lerp(t, (float) prevPos.y, (float) pos.y);
    }

    public double getZ(float t) {
        return MathHelper.lerp(t, (float) prevPos.z, (float) pos.z);
    }

    public double getYaw(float t) {
        return MathHelper.lerp(t, lastYaw, yaw);
    }

    public double getPitch(float t) {
        return MathHelper.lerp(t, lastPitch, pitch);
    }
}
