/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/vexon-client).
 * Copyright (c) Meteor Development.
 */

package me.vexon.client.modules.vexonesp;

import me.vexon.client.events.render.Render3DEvent;
import me.vexon.client.events.world.TickEvent;
import me.vexon.client.renderer.ShapeMode;
import me.vexon.client.settings.*;
import me.vexon.client.systems.modules.Categories;
import me.vexon.client.systems.modules.Module;
import me.vexon.client.utils.player.ChatUtils;
import me.vexon.client.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import me.vexon.client.utils.render.RenderUtils;

import java.util.HashSet;
import java.util.Set;

public class ClusterFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    // General
    private final Setting<Boolean> tracers = sgRender.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draws a line from the crosshair to the cluster.")
            .defaultValue(true)
            .build());

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
            .name("notify")
            .description("Send chat message when a cluster/budding amethyst is found.")
            .defaultValue(true)
            .build());

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build());

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color of the box.")
            .defaultValue(new SettingColor(147, 112, 219, 75)) // Medium Purple
            .build());

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color of the box.")
            .defaultValue(new SettingColor(147, 112, 219, 255))
            .build());

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
            .name("tracer-color")
            .description("The color of the tracer line.")
            .defaultValue(new SettingColor(147, 112, 219, 255))
            .build());

    private final Set<BlockPos> clusters = new HashSet<>();
    private final Set<ChunkPos> scannedChunks = new HashSet<>();

    public ClusterFinder() {
        super(Category.VEXON_ESP, "Cluster Finder", "Finds Amethyst Clusters and Budding Amethyst.");
    }

    @Override
    public void onDeactivate() {
        clusters.clear();
        scannedChunks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world == null)
            return;

        // Scan loaded chunks
        int renderDistance = mc.options.getViewDistance().getValue();
        ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());

        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int z = -renderDistance; z <= renderDistance; z++) {
                ChunkPos pos = new ChunkPos(playerChunk.x + x, playerChunk.z + z);
                if (!scannedChunks.contains(pos) && mc.world.getChunkManager().isChunkLoaded(pos.x, pos.z)) {
                    scanChunk(pos);
                    scannedChunks.add(pos);
                }
            }
        }
    }

    private void scanChunk(ChunkPos pos) {
        WorldChunk chunk = mc.world.getChunk(pos.x, pos.z);
        if (chunk == null)
            return;

        int minY = mc.world.getBottomY();
        int maxY = minY + mc.world.getHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos bp = pos.getBlockPos(x, y, z);
                    var state = chunk.getBlockState(bp);
                    if (state.isOf(Blocks.BUDDING_AMETHYST) || state.isOf(Blocks.AMETHYST_CLUSTER)) {
                        synchronized (clusters) {
                            if (clusters.add(bp) && notify.get()) {
                                ChatUtils.info("Found Amethyst at " + bp.toShortString());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        synchronized (clusters) {
            for (BlockPos pos : clusters) {
                event.renderer.box(pos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
                if (tracers.get()) {
                    event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tracerColor.get());
                }
            }
        }
    }
}
