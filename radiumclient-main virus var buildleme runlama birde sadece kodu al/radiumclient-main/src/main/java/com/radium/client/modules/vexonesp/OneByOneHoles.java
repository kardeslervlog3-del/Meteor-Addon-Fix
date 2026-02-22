package me.vexon.client.modules.vexonesp;

import me.vexon.client.systems.modules.Categories;
import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.events.world.BlockUpdateEvent;
import me.vexon.client.events.world.ChunkDataEvent;
import me.vexon.client.renderer.ShapeMode;
import me.vexon.client.settings.BoolSetting;
import me.vexon.client.settings.ColorSetting;
import me.vexon.client.settings.EnumSetting;
import me.vexon.client.settings.Setting;
import me.vexon.client.settings.SettingGroup;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.Utils;
import me.vexon.client.utils.render.color.Color;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class OneByOneHoles extends Module {
    private final SettingGroup sgGeneral = settings.createGroup("General");

    private final Setting<SettingColor> holeColor = sgGeneral.add(new ColorSetting.Builder()
            .name("hole-color")
            .description("Color for 1x1x1 holes.")
            .defaultValue(new SettingColor(255, 0, 0, 100))
            .build());

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("Render mode for 1x1x1 holes.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draw tracers to 1x1x1 holes.")
            .defaultValue(false)
            .build());

    private final Setting<SettingColor> tracerColor = sgGeneral.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("1x1x1 hole tracer color.")
            .defaultValue(new SettingColor(255, 0, 0, 200))
            .visible(tracers::get)
            .build());

    private final Set<BlockPos> oneByOneHoles = new HashSet<>();

    public OneByOneHoles() {
        super(Category.VEXON_ESP, "One By One Holes", "Highlights 1x1x1 air holes that are likely player-made.");
    }

    @Override
    public void onActivate() {
        if (mc.world == null)
            return;

        oneByOneHoles.clear();
        for (var chunk : Utils.chunks()) {
            if (chunk instanceof WorldChunk wc)
                scanChunk(wc);
        }
    }

    @Override
    public void onDeactivate() {
        oneByOneHoles.clear();
    }

    @EventHandler
    private void onChunkLoad(ChunkDataEvent event) {
        if (event.chunk() instanceof WorldChunk wc)
            scanChunk(wc);
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        BlockPos pos = event.pos;

        if (isOneByOneHole(pos)) {
            oneByOneHoles.add(pos.toImmutable());
        }

        for (Direction dir : Direction.values()) {
            BlockPos n = pos.offset(dir);
            if (isOneByOneHole(n))
                oneByOneHoles.add(n.toImmutable());
            else
                oneByOneHoles.remove(n);
        }
    }

    private void scanChunk(WorldChunk chunk) {
        if (mc.world == null || mc.player == null)
            return;

        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int endX = startX + 16;
        int endZ = startZ + 16;

        int bottomY = chunk.getBottomY();
        int topY = bottomY + chunk.getHeight();

        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                for (int y = bottomY; y < topY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!isOneByOneHole(pos))
                        continue;

                    oneByOneHoles.add(pos.toImmutable());
                }
            }
        }
    }

    private boolean isOneByOneHole(BlockPos pos) {
        if (mc.world == null)
            return false;

        BlockState state = mc.world.getBlockState(pos);
        if (!state.isAir())
            return false;

        // Check if surrounded by blocks
        for (Direction dir : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(dir)).isAir())
                return false;
        }

        return true;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (oneByOneHoles.isEmpty())
            return;

        Color color = holeColor.get();
        Vec3d cameraPos = mc.gameRenderer.getCamera().getCameraPos();

        for (BlockPos pos : oneByOneHoles) {
            event.renderer.box(pos, color, color, shapeMode.get(), 0);

            if (tracers.get()) {
                event.renderer.line(cameraPos.x, cameraPos.y, cameraPos.z, pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, tracerColor.get());
            }
        }
    }
}
