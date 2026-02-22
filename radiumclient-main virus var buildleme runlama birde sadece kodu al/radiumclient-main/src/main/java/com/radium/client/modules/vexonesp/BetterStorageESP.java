package me.vexon.client.modules.vexonesp;

import me.vexon.client.client.VexonClient;
import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.NumberSetting;
import me.vexon.client.gui.settings.ColorSetting;
import me.vexon.client.modules.Module;
import me.vexon.client.gui.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.*;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.Color;

public class BetterStorageESP extends Module {

        private final NumberSetting fillAlpha = new NumberSetting("Fill Alpha", 125.0, 0.0, 255.0, 1.0);
        private final NumberSetting outlineAlpha = new NumberSetting("Outline Alpha", 255.0, 0.0, 255.0, 1.0);
        private final BoolSetting tracers = new BoolSetting("Tracers", false);

        private final BoolSetting chests = new BoolSetting("Chests", true);
        private final BoolSetting enderChests = new BoolSetting("Ender Chests", true);
        private final BoolSetting shulkerBoxes = new BoolSetting("Shulker Boxes", true);
        private final BoolSetting spawners = new BoolSetting("Spawners", true);
        private final BoolSetting furnaces = new BoolSetting("Furnaces", true);
        private final BoolSetting barrels = new BoolSetting("Barrels", true);
        private final BoolSetting enchantingTables = new BoolSetting("Enchanting Tables", true);
        private final BoolSetting hoppers = new BoolSetting("Hoppers", true);

        private final ColorSetting chestColor = new ColorSetting("Chest", new Color(156, 91, 0).getRGB());
        private final ColorSetting enderChestColor = new ColorSetting("Ender Chest", new Color(117, 0, 255).getRGB());
        private final ColorSetting shulkerBoxColor = new ColorSetting("Shulker Box", new Color(134, 0, 158).getRGB());
        private final ColorSetting spawnerColor = new ColorSetting("Spawner", new Color(138, 126, 166).getRGB());
        private final ColorSetting furnaceColor = new ColorSetting("Furnace", new Color(100, 100, 100).getRGB());
        private final ColorSetting barrelColor = new ColorSetting("Barrel", new Color(100, 75, 50).getRGB());
        private final ColorSetting enchantColor = new ColorSetting("Enchanting Table", new Color(200, 0, 0).getRGB());
        private final ColorSetting hopperColor = new ColorSetting("Hopper", new Color(80, 80, 80).getRGB());

        public BetterStorageESP() {
                super("BetterStorageESP", "Advanced storage ESP.", Category.VEXON_ESP);
                this.addSettings(fillAlpha, outlineAlpha, tracers, chests, enderChests, shulkerBoxes, spawners,
                                furnaces, barrels, enchantingTables, hoppers, chestColor, enderChestColor,
                                shulkerBoxColor, spawnerColor, furnaceColor, barrelColor, enchantColor, hopperColor);
        }

        @Override
        public void onEnable() {
                WorldRenderEvents.AFTER_ENTITIES.register(this::onRender3D);
                super.onEnable();
        }

        private void onRender3D(WorldRenderContext context) {
                if (!this.isEnabled() || mc.world == null || mc.player == null)
                        return;

                double s = 0.15;
                int fillA = fillAlpha.getValue().intValue();
                int lineA = outlineAlpha.getValue().intValue();
                int vd = mc.options.getViewDistance().getValue();
                ChunkPos playerChunk = mc.player.getChunkPos();
                ClientChunkManager cm = mc.world.getChunkManager();

                Vec3d camPos = context.camera().getPos();
                Vec3d tracerStart = RenderUtils.getCameraPos();

                for (int dx = -vd; dx <= vd; dx++) {
                        for (int dz = -vd; dz <= vd; dz++) {
                                WorldChunk chunk = cm.getChunk(playerChunk.x + dx, playerChunk.z + dz, ChunkStatus.FULL,
                                                false);
                                if (chunk == null)
                                        continue;

                                for (BlockEntity be : chunk.getBlockEntities().values()) {
                                        Color baseColor = getBlockEntityColor(be);
                                        if (baseColor == null)
                                                continue;

                                        BlockPos pos = be.getPos();
                                        Color fillColor = new Color(baseColor.getRed(), baseColor.getGreen(),
                                                        baseColor.getBlue(), fillA);
                                        Color lineColor = new Color(baseColor.getRed(), baseColor.getGreen(),
                                                        baseColor.getBlue(), lineA);

                                        Box box = new Box(pos.getX() + s, pos.getY() + s, pos.getZ() + s,
                                                        pos.getX() + 1.0 - s, pos.getY() + 1.0 - s,
                                                        pos.getZ() + 1.0 - s);

                                        context.matrixStack().push();
                                        context.matrixStack().translate(-camPos.x, -camPos.y, -camPos.z);

                                        RenderUtils.drawBox(context.matrixStack(), box, fillColor.getRGB(), false);
                                        RenderUtils.drawBox(context.matrixStack(), box, lineColor.getRGB(), true);

                                        if (tracers.getValue()) {
                                                RenderUtils.renderLine(context.matrixStack(), lineColor, tracerStart,
                                                                new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5,
                                                                                pos.getZ() + 0.5));
                                        }

                                        context.matrixStack().pop();
                                }
                        }
                }
        }

        private Color getBlockEntityColor(BlockEntity be) {
                if (be instanceof ChestBlockEntity && chests.getValue())
                        return new Color(chestColor.getValue());
                if (be instanceof EnderChestBlockEntity && enderChests.getValue())
                        return new Color(enderChestColor.getValue());
                if (be instanceof ShulkerBoxBlockEntity && shulkerBoxes.getValue())
                        return new Color(shulkerBoxColor.getValue());
                if (be instanceof MobSpawnerBlockEntity && spawners.getValue())
                        return new Color(spawnerColor.getValue());
                if (be instanceof AbstractFurnaceBlockEntity && furnaces.getValue())
                        return new Color(furnaceColor.getValue());
                if (be instanceof BarrelBlockEntity && barrels.getValue())
                        return new Color(barrelColor.getValue());
                if (be instanceof EnchantingTableBlockEntity && enchantingTables.getValue())
                        return new Color(enchantColor.getValue());
                if (be instanceof HopperBlockEntity && hoppers.getValue())
                        return new Color(hopperColor.getValue());
                return null;
        }
}
