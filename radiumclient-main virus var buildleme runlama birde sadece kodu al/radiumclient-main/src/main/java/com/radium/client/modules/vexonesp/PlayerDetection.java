package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.world.TickEvent;
import me.vexon.client.settings.*;
import me.vexon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public class PlayerDetection extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> chatNotify = sgGeneral.add(new BoolSetting.Builder()
            .name("chat-notify")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> disconnect = sgGeneral.add(new BoolSetting.Builder()
            .name("disconnect")
            .defaultValue(false)
            .build());

    private final Set<String> detectedPlayers = new HashSet<>();

    public PlayerDetection() {
        super(Category.VEXON_ESP, "Player Detection", "Notifies or disconnects when players are detected (Vexon).");
    }

    @Override
    public void onActivate() {
        detectedPlayers.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null || mc.player == null)
            return;

        boolean foundNew = false;
        Set<String> currentPlayers = new HashSet<>();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player)
                continue;

            String name = player.getName().getString();
            currentPlayers.add(name);

            if (!detectedPlayers.contains(name)) {
                foundNew = true;
                detectedPlayers.add(name);
                if (chatNotify.get())
                    info("Detected player: " + name);
            }
        }

        if (foundNew) {
            if (disconnect.get()) {
                mc.getNetworkHandler().getConnection().disconnect(Text.literal("Player Detected (Vexon Protection)"));
            }
        }

        detectedPlayers.retainAll(currentPlayers);
    }
}
