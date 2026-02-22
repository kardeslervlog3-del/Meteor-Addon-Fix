package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.world.TickEvent;
import me.vexon.client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class HomeReset extends Module {
    public HomeReset() {
        super(Category.VEXON_ESP, "Home Reset", "Automatically resets home position via command detection (Vexon).");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        // Implementation depends on server-side message detection or manual triggers
    }
}
