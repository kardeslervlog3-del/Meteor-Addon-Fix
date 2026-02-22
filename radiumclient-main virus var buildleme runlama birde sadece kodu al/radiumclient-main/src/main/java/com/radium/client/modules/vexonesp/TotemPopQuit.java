package me.vexon.client.modules.vexonesp;

import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.modules.Module;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;

public class TotemPopQuit extends Module {

    private final BoolSetting quitOnPop = new BoolSetting("Quit on Pop", true);

    public TotemPopQuit() {
        super("TotemPopQuit", "Disconnects when you pop a totem.", Category.VEXON_ESP);
        this.addSettings(quitOnPop);
    }

    public void onPacketReceive(Object packet) {
        if (!(packet instanceof EntityStatusS2CPacket statusPacket))
            return;
        if (statusPacket.getStatus() != 35)
            return;

        if (statusPacket.getEntity(mc.world) == mc.player) {
            if (quitOnPop.getValue()) {
                mc.getNetworkHandler().getConnection()
                        .disconnect(Text.literal("Totem Pop Detected (Vexon Protection)"));
            }
        }
    }
}
