package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.renderer.ShapeMode;
import me.vexon.client.settings.*;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VineESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> minVines = sgGeneral.add(new IntSetting.Builder()
            .name("min-vines")
            .description("Minimum number of vines in a chunk to highlight it.")
            .defaultValue(1)
            .min(1)
            .sliderRange(1, 50)
            .build());

    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>()
            .name("render-mode")
            .description("How to render the ESP.")
            .defaultValue(RenderMode.FullBox)
            .build());

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
            .name("color")
            .description("Color of the highlight.")
            .defaultValue(new SettingColor(0, 255, 0, 80))
            .build());

    private final List<ChunkPos> highlightedChunks = Collections.synchronizedList(new ArrayList<>());
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public VineESP() {
        super(Category.VEXON_ESP, "Vine ESP", "Highlights chunks with grounded long vines (Vexon).");
    }

    @Override
    public void onActivate() {
        highlightedChunks.clear();
    }

    @Override
    public void onDeactivate() {
        highlightedChunks.clear();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null)
            return;

        synchronized (highlightedChunks) {
            for (ChunkPos pos : highlightedChunks) {
                double x = pos.getStartX();
                double z = pos.getStartZ();

                if (renderMode.get() == RenderMode.FullBox) {
                    event.renderer.box(x, mc.world.getBottomY(), z, x + 16, mc.world.getTopYInclusive() + 1, z + 16,
                            color.get(),
                            color.get(), ShapeMode.Both, 0);
                } else {
                    // Ground Square mode: render a flat square on the ground surface
                    int surfaceY = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, (int) x + 8, (int) z + 8);
                    event.renderer.box(x, surfaceY, z, x + 16, surfaceY + 0.05, z + 16, color.get(), color.get(),
                            ShapeMode.Both, 0);
                }
            }
        }

        executor.execute(() -> {
            List<ChunkPos> newHighlights = new ArrayList<>();
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkPos pos = new ChunkPos(mc.player.getChunkPos().x + x, mc.player.getChunkPos().z + z);
                    if (mc.world.getChunkManager().isChunkLoaded(pos.x, pos.z)) {
                        Chunk chunk = mc.world.getChunk(pos.x, pos.z);
                        if (countVines(chunk) >= minVines.get()) {
                            newHighlights.add(pos);
                        }
                    }
                }
            }
            synchronized (highlightedChunks) {
                highlightedChunks.clear();
                highlightedChunks.addAll(newHighlights);
            }
        });
    }

    private int countVines(Chunk chunk) {
        int count = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = mc.world.getBottomY(); y < mc.world.getTopYInclusive() + 1; y++) {
                    mutable.set(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z);
                    if (chunk.getBlockState(mutable).isOf(Blocks.VINE)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public enum RenderMode {
        FullBox,
        GroundSquare
    }
}
