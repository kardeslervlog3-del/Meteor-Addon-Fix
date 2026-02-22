package me.vexon.client.modules.vexonesp.chunkfinder;

@FunctionalInterface
public interface ChunkFlagEvaluator<T> {
    boolean shouldFlag(T data);
}
