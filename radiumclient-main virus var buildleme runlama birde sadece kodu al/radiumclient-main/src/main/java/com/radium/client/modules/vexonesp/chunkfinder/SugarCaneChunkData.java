package me.vexon.client.modules.vexonesp.chunkfinder;

public record SugarCaneChunkData(int qualifyingColumns, int maxHeight) {
    public boolean hasSignal(int minColumns) {
        return qualifyingColumns >= minColumns;
    }
}
