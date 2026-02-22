package me.vexon.client.modules.vexonesp.chunkfinder;

import net.minecraft.util.math.ChunkPos;

import java.util.Map;

@FunctionalInterface
public interface ChunkFlagContextEvaluator<T> {
    boolean shouldFlag(ChunkPos chunkPos, T data, Map<ChunkPos, T> allData);
}
