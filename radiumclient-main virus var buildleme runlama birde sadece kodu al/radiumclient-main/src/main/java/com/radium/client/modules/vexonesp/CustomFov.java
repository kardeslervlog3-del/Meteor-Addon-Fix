package me.vexon.client.modules.vexonesp;

import me.vexon.client.gui.settings.NumberSetting;
import me.vexon.client.modules.Module;

public final class CustomFov extends Module {

    private final NumberSetting fov = new NumberSetting("FOV", 110.0, 30.0, 160.0, 1.0);

    public CustomFov() {
        super("CustomFOV", "Changes your field of view.", Category.VEXON_ESP);
        this.addSettings(fov);
    }

    public double getFov() {
        return isEnabled() ? fov.getValue() : 0;
    }
}
