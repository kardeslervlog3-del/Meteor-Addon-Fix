package me.vexon.client.modules.vexonesp;

import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.NumberSetting;
import me.vexon.client.gui.settings.StringSetting;
import me.vexon.client.modules.Category;
import me.vexon.client.modules.Module;
import net.minecraft.client.network.PlayerListEntry;

public class NameProtect extends Module {

    public final BoolSetting hideSelf = new BoolSetting("Protect Self", true);
    public final BoolSetting replaceName = new BoolSetting("Replace Name", true);
    public final StringSetting customName = new StringSetting("Custom Name", "Protected");
    public final BoolSetting protectTab = new BoolSetting("Protect Tab", true);
    public final BoolSetting protectChat = new BoolSetting("Protect Chat", true);
    public final BoolSetting protectOverlay = new BoolSetting("Protect Overlay", true);
    public final NumberSetting range = new NumberSetting("Range", 64.0, 0.0, 128.0, 1.0);
    public final BoolSetting hideFriends = new BoolSetting("Protect Friends", false);
    public final BoolSetting hideSkins = new BoolSetting("Hide Skins", false);

    public NameProtect() {
        super("NameProtect", "Hides or replaces names.", Category.VEXON_ESP);
        this.addSettings(hideSelf, replaceName, customName, protectTab, protectChat, protectOverlay, range, hideFriends,
                hideSkins);
    }

    public String getName(String original) {
        if (!isEnabled() || !hideSelf.getValue() || !replaceName.getValue())
            return original;
        return customName.getValue();
    }

    public boolean skinProtect() {
        return isEnabled() && hideSkins.getValue();
    }

    public String replaceNameInText(String input) {
        if (!isEnabled() || input == null || input.isEmpty())
            return input;

        String result = input;
        String replacement = customName.getValue();
        String selfName = mc.getSession().getUsername();

        if (hideSelf.getValue()) {
            result = result.replace(selfName, replacement);
        }

        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                String name = entry.getProfile().getName();
                if (name.equals(selfName))
                    continue;

                // Simplified friend check
                boolean isFriend = false;

                if (isFriend && hideFriends.getValue()) {
                    result = result.replace(name, replacement);
                } else if (!isFriend && name.length() >= 3) {
                    result = result.replace(name, replacement);
                }
            }
        }

        return result;
    }
}
