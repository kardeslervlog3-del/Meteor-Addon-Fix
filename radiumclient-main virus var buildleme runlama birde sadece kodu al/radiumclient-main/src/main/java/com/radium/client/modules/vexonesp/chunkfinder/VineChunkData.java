package me.vexon.client.modules.vexonesp.chunkfinder;

public record VineChunkData(int groundedCount, int maxLength) {
    public boolean hasSignal(int threshold) {
        return groundedCount >= threshold;
    }
}
