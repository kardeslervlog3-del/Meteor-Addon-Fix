package me.vexon.client.modules.vexonesp;

import me.vexon.client.client.VexonClient;
import me.vexon.client.gui.settings.BoolSetting;
import me.vexon.client.gui.settings.NumberSetting;
import me.vexon.client.modules.Module;
import me.vexon.client.gui.RenderUtils;
import me.vexon.client.modules.vexonesp.chunkfinder.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

import java.awt.Color;

public class ChunkFinder extends Module {

        private final NumberSetting threadPoolSize = new NumberSetting("Threads",
                        (double) Runtime.getRuntime().availableProcessors(), 1.0, 32.0, 1.0);
        private final NumberSetting chunkBuffer = new NumberSetting("Chunk Buffer", 512.0, 1.0, 2048.0, 32.0);
        private final BoolSetting chatFeedback = new BoolSetting("Chat Feedback", true);
        private final BoolSetting playSound = new BoolSetting("Play Sound", true);

        private final BoolSetting findClusters = new BoolSetting("Find Clusters", true);
        private final NumberSetting clusterThreshold = new NumberSetting("Cluster Threshold", 5.0, 1.0, 20.0, 1.0);
        private final NumberSetting scannerMinY = new NumberSetting("Min Y", -64.0, -64.0, 320.0, 16.0);
        private final NumberSetting scannerMaxY = new NumberSetting("Max Y", 32.0, -64.0, 320.0, 16.0);

        private final BoolSetting findKelp = new BoolSetting("Find Kelp", true);
        private final NumberSetting minKelpColumns = new NumberSetting("Min Kelp Columns", 5.0, 1.0, 20.0, 1.0);

        private final BoolSetting findCactus = new BoolSetting("Find Cactus", true);
        private final NumberSetting minCactusHeight = new NumberSetting("Min Cactus Height", 5.0, 1.0, 10.0, 1.0);

        private final BoolSetting findSugarCane = new BoolSetting("Find Sugar Cane", true);
        private final NumberSetting minSugarCaneHeight = new NumberSetting("Min Sugar Cane Height", 5.0, 1.0, 10.0,
                        1.0);

        private final BoolSetting findBeehive = new BoolSetting("Find Beehive", true);

        private ChunkFinderRuntime<GrowthChunkData> runtime;
        private NotificationLimiter<ChunkPos> notificationLimiter;

        public ChunkFinder() {
                super("ChunkFinder", "Flags chunks containing specific growths.", Category.VEXON_ESP);
                this.addSettings(threadPoolSize, chunkBuffer, chatFeedback, playSound, findClusters, clusterThreshold,
                                scannerMinY, scannerMaxY, findKelp, minKelpColumns, findCactus, minCactusHeight,
                                findSugarCane, minSugarCaneHeight, findBeehive);
        }

        @Override
        public void onEnable() {
                notificationLimiter = new NotificationLimiter<>(5, 10000);

                runtime = new ChunkFinderRuntime<>(
                                mc,
                                new GrowthChunkScanner(
                                                mc.world,
                                                scannerMinY.getValue().intValue(),
                                                scannerMaxY.getValue().intValue(),
                                                () -> 10,
                                                () -> 10,
                                                minCactusHeight.getValue()::intValue,
                                                minSugarCaneHeight.getValue()::intValue),
                                data -> data.signalCount(
                                                clusterThreshold.getValue().intValue(),
                                                findKelp.getValue() && data.hasKelpSignal(
                                                                minKelpColumns.getValue().intValue(), 3, 0.5),
                                                findCactus.getValue() && data.hasCactusSignal(5),
                                                findSugarCane.getValue() && data.hasSugarCaneSignal(5),
                                                findBeehive.getValue() && data.hasBeehiveSignal()) > 0,
                                (chunkPos, data) -> {
                                        if (notificationLimiter.shouldNotify(chunkPos, System.currentTimeMillis())) {
                                                if (chatFeedback.getValue()) {
                                                        VexonClient.info("Found growth at " + chunkPos.x + ", "
                                                                        + chunkPos.z);
                                                }
                                                if (playSound.getValue()) {
                                                        mc.world.playSoundFromEntity(mc.player, mc.player,
                                                                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                                                                        SoundCategory.AMBIENT, 3.0F, 1.0F);
                                                }
                                        }
                                },
                                threadPoolSize.getValue().intValue());

                runtime.activate();
                runtime.startInitialScan();

                WorldRenderEvents.AFTER_ENTITIES.register(this::onRender);
                super.onEnable();
        }

        @Override
        public void onDisable() {
                if (runtime != null) {
                        runtime.deactivate();
                }
                super.onDisable();
        }

        private void onRender(WorldRenderContext context) {
                if (!this.isEnabled() || runtime == null || mc.player == null || mc.world == null)
                        return;

                double h = 0.1;
                Vec3d camPos = context.camera().getPos();

                for (ChunkPos chunkPos : runtime.flaggedChunks()) {
                        GrowthChunkData data = runtime.dataFor(chunkPos);
                        if (data == null)
                                continue;

                        Color color = new Color(0, 191, 255, 100);

                        double x = chunkPos.getStartX();
                        double z = chunkPos.getStartZ();
                        double y = mc.world.getBottomY();

                        Box box = new Box(x, y, z, x + 16, y + h, z + 16);

                        context.matrixStack().push();
                        context.matrixStack().translate(-camPos.x, -camPos.y, -camPos.z);
                        RenderUtils.drawBox(context.matrixStack(), box, color.getRGB(), false);
                        RenderUtils.drawBox(context.matrixStack(), box, color.getRGB(), true);
                        context.matrixStack().pop();
                }
        }

        @Override
        public String getInfoString() {
                return runtime != null ? String.valueOf(runtime.flaggedCount()) : null;
        }
}
