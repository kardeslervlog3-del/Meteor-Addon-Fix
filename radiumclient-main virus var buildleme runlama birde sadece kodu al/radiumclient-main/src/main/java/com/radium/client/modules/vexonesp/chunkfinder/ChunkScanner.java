package me.vexon.client.modules.vexonesp.chunkfinder;

import net.minecraft.world.chunk.WorldChunk;

@FunctionalInterface
public interface ChunkScanner<T> {
    T scan(WorldChunk chunk);
}
