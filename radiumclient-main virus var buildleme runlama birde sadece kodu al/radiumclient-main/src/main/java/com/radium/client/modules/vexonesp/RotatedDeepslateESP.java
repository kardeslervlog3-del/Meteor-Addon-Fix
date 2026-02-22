package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.events.world.BlockUpdateEvent;
import me.vexon.client.events.world.ChunkDataEvent;
import me.vexon.client.renderer.ShapeMode;
import me.vexon.client.settings.BoolSetting;
import me.vexon.client.settings.ColorSetting;
import me.vexon.client.settings.EnumSetting;
import me.vexon.client.settings.IntSetting;
import me.vexon.client.settings.Setting;
import me.vexon.client.settings.SettingGroup;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.Utils;
import me.vexon.client.utils.render.color.Color;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RotatedDeepslateESP extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");
    private final SettingGroup sgFiltering = settings.createGroup("Block Types");
    private final SettingGroup sgRange = settings.createGroup("Range");

    private final Setting<SettingColor> deepslateColor = sgGeneral.add(new ColorSetting.Builder()
            .name("esp-color")
            .description("The color of the ESP box and tracers.")
            .defaultValue(new SettingColor(255, 0, 255, 100))
            .build());

    private final Setting<ShapeMode> deepslateShapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<Boolean> ignoreExposed = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-exposed")
            .description("Only show blocks that are completely buried (not touching air/water).")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draw lines from your crosshair/camera to the blocks.")
            .defaultValue(false)
            .build());

    private final Setting<Boolean> includeRegular = sgFiltering.add(new BoolSetting.Builder()
            .name("regular-deepslate")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> includeBricks = sgFiltering.add(new BoolSetting.Builder()
            .name("deepslate-bricks")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> includeTiles = sgFiltering.add(new BoolSetting.Builder()
            .name("deepslate-tiles")
            .defaultValue(true)
            .build());

    private final Setting<Integer> minY = sgRange.add(new IntSetting.Builder()
            .name("min-y")
            .defaultValue(-64)
            .range(-64, 320)
            .sliderRange(-64, 320)
            .build());

    private final Setting<Integer> maxY = sgRange.add(new IntSetting.Builder()
            .name("max-y")
            .defaultValue(128)
            .range(-64, 320)
            .sliderRange(-64, 320)
            .build());

    private final Setting<Integer> renderDistance = sgRange.add(new IntSetting.Builder()
            .name("render-distance")
            .description("Max render distance in blocks.")
            .defaultValue(256)
            .range(32, 1024)
            .sliderRange(32, 1024)
            .build());

    private final Set<BlockPos> rotatedPositions = ConcurrentHashMap.newKeySet();
    private ExecutorService threadPool;

    public RotatedDeepslateESP() {
        super(Category.VEXON_ESP, "Rotated Deepslate ESP", "Flags chunks with rotated deepslate.");
    }

    @Override
    public void onActivate() {
        rotatedPositions.clear();
        threadPool = Executors.newFixedThreadPool(1);

        if (mc.world != null) {
            for (var chunk : Utils.chunks()) {
                scanChunk(chunk);
            }
        }
    }

    @Override
    public void onDeactivate() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
        rotatedPositions.clear();
    }

    @EventHandler
    private void onChunkLoad(ChunkDataEvent event) {
        scanChunk(event.chunk());
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        if (mc.world == null)
            return;

        if (isRotatedDeepslate(mc.world.getBlockState(event.pos), event.pos, mc.world)) {
            rotatedPositions.add(event.pos.toImmutable());
        } else {
            rotatedPositions.remove(event.pos);
        }
    }

    private void scanChunk(Chunk chunk) {
        if (threadPool == null)
            return;

        threadPool.execute(() -> {
            if (mc.world == null)
                return;

            ChunkPos cp = chunk.getPos();
            int startX = cp.getStartX();
            int startZ = cp.getStartZ();

            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = minY.get(); y <= maxY.get(); y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = chunk.getBlockState(pos);

                        if (isRotatedDeepslate(state, pos, mc.world)) {
                            rotatedPositions.add(pos.toImmutable());
                        }
                    }
                }
            }
        });
    }

    private boolean isRotatedDeepslate(BlockState state, BlockPos pos, World world) {
        if (state.isAir())
            return false;

        boolean matches = false;
        if (includeRegular.get() && state.isOf(Blocks.DEEPSLATE))
            matches = true;
        else if (includeBricks.get() && state.isOf(Blocks.DEEPSLATE_BRICKS))
            matches = true;
        else if (includeTiles.get() && state.isOf(Blocks.DEEPSLATE_TILES))
            matches = true;

        if (!matches)
            return false;

        // Check rotation
        if (state.contains(Properties.AXIS)) {
            Direction.Axis axis = state.get(Properties.AXIS);
            if (axis == Direction.Axis.Y)
                return false;
        } else {
            return false;
        }

        if (ignoreExposed.get()) {
            for (Direction dir : Direction.values()) {
                if (world.getBlockState(pos.offset(dir)).isAir())
                    return false;
            }
        }

        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (rotatedPositions.isEmpty())
            return;

        Color color = deepslateColor.get();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        for (BlockPos pos : rotatedPositions) {
            double distSq = pos.getSquaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            if (distSq > renderDistance.get() * renderDistance.get())
                continue;

            event.renderer.box(pos, color, color, deepslateShapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(cameraPos.x, cameraPos.y, cameraPos.z, pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, color);
            }
        }
    }
}
