package me.vexon.client.modules.vexonesp;

import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.StringSetting;
import me.vexon.client.modules.Module;

public class VexonScoreboard extends Module {

        public final BoolSetting showRealMoney = new BoolSetting("real-money", false);
        public final BoolSetting showRealShards = new BoolSetting("real-shards", false);
        public final BoolSetting showRealKills = new BoolSetting("real-kills", false);
        public final BoolSetting showRealDeaths = new BoolSetting("real-deaths", false);
        public final BoolSetting showRealKeyall = new BoolSetting("real-keyall", true);
        public final BoolSetting showRealPlaytime = new BoolSetting("real-playtime", false);
        public final BoolSetting showRealTeam = new BoolSetting("real-team", false);
        public final BoolSetting hideRegion = new BoolSetting("hide-region", false);

        public final StringSetting moneyValue = new StringSetting("money", "Vexon on top");
        public final StringSetting shardsValue = new StringSetting("shards", "2.3K");
        public final StringSetting killsValue = new StringSetting("kills", "503");
        public final StringSetting deathsValue = new StringSetting("deaths", "421");
        public final StringSetting keyallValue = new StringSetting("key-all", "67m 67s");
        public final StringSetting playtimeValue = new StringSetting("playtime", "22d 9h");
        public final StringSetting teamValue = new StringSetting("team", "Vexon Client");

        public VexonScoreboard() {
                super("VexonScoreboard", "Shows a fake scoreboard with custom values.", Category.VEXON_ESP);
                this.addSettings(showRealMoney, showRealShards, showRealKills, showRealDeaths, showRealKeyall,
                                showRealPlaytime, showRealTeam, hideRegion, moneyValue, shardsValue, killsValue,
                                deathsValue, keyallValue, playtimeValue, teamValue);
        }
}
