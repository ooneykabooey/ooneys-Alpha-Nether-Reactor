package net.minecraft.src;

public class NetherReactorPattern {
    private final int gold = Block.blockGold.blockID;
    private final int cobblestone = Block.cobblestone.blockID;
    private final int netherReactor = Block.netherReactor.blockID;

    private int[][][] types = 	{
            // Level 0
            {
                    {gold, cobblestone, gold},
                    {cobblestone, cobblestone, cobblestone},
                    {gold, cobblestone, gold}
            },
            // Level 1
            {
                    {cobblestone, 0, cobblestone},
                    {0, netherReactor, 0},
                    {cobblestone, 0, cobblestone}
            },
            // Level 2
            {
                    {0, cobblestone, 0},
                    {cobblestone, cobblestone, cobblestone},
                    {0, cobblestone, 0}
            }
    };

    public void setTileAt(int level, int x, int z, int tile) {
        types[level][x][z] = tile;
    }

    public int getTileAt(int level, int x, int z) {
        return types[level][x][z];
    }
}
