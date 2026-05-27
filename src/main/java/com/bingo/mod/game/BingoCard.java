package com.bingo.mod.game;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Represents a 5x5 Bingo card for a single player.
 * The center cell (2,2) is always FREE.
 */
public class BingoCard {

    public static final int SIZE = 5;
    public static final int FREE_CENTER = SIZE / 2; // index 2

    // All possible bingo items - a curated mix of Minecraft items across all biomes/dimensions
    private static final List<Item> ITEM_POOL = Arrays.asList(
        Items.DIAMOND, Items.EMERALD, Items.GOLD_INGOT, Items.IRON_INGOT,
        Items.COAL, Items.REDSTONE, Items.LAPIS_LAZULI, Items.QUARTZ,
        Items.OAK_LOG, Items.BIRCH_LOG, Items.SPRUCE_LOG, Items.JUNGLE_LOG,
        Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.CHERRY_LOG, Items.MANGROVE_LOG,
        Items.APPLE, Items.MELON_SLICE, Items.PUMPKIN, Items.BREAD,
        Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_SALMON, Items.BAKED_POTATO,
        Items.BOW, Items.ARROW, Items.SHIELD, Items.LEATHER_CHESTPLATE,
        Items.IRON_SWORD, Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL,
        Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE,
        Items.BOOK, Items.PAPER, Items.FEATHER, Items.INK_SAC,
        Items.BONE, Items.STRING, Items.SPIDER_EYE, Items.GUNPOWDER,
        Items.LEATHER, Items.WOOL, Items.WHEAT, Items.SUGAR_CANE,
        Items.SAND, Items.GRAVEL, Items.FLINT, Items.CLAY_BALL,
        Items.CACTUS, Items.DEAD_BUSH, Items.VINE, Items.LILY_PAD,
        Items.MUSHROOM_STEW, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM,
        Items.ENDER_PEARL, Items.BLAZE_ROD, Items.NETHER_STAR,
        Items.OBSIDIAN, Items.GLOWSTONE_DUST, Items.NETHER_BRICK,
        Items.SEA_LANTERN, Items.PRISMARINE_SHARD, Items.COD, Items.SALMON,
        Items.TURTLE_EGG, Items.KELP, Items.DRIED_KELP, Items.SEAGRASS,
        Items.SLIME_BALL, Items.MAGMA_CREAM, Items.GHAST_TEAR,
        Items.RABBIT_HIDE, Items.RABBIT_FOOT, Items.MUTTON, Items.PORKCHOP,
        Items.CARROT, Items.POTATO, Items.BEETROOT, Items.SWEET_BERRIES,
        Items.SNOWBALL, Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE,
        Items.ANVIL, Items.FURNACE, Items.CRAFTING_TABLE, Items.CHEST,
        Items.TORCH, Items.LANTERN, Items.CAMPFIRE, Items.SOUL_SAND,
        Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP
    );

    private final Item[][] grid; // [row][col]
    private final boolean[][] collected; // [row][col]
    private final UUID playerUUID;

    public BingoCard(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.grid = new Item[SIZE][SIZE];
        this.collected = new boolean[SIZE][SIZE];

        generateCard();
    }

    /**
     * Create from network data (client-side reconstruction)
     */
    public BingoCard(UUID playerUUID, Item[][] grid, boolean[][] collected) {
        this.playerUUID = playerUUID;
        this.grid = grid;
        this.collected = collected;
    }

    private void generateCard() {
        List<Item> shuffled = new ArrayList<>(ITEM_POOL);
        Collections.shuffle(shuffled);

        // Fill 24 cells (center is FREE)
        int idx = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (row == FREE_CENTER && col == FREE_CENTER) {
                    grid[row][col] = Items.NETHER_STAR; // placeholder for FREE
                    collected[row][col] = true; // FREE is always collected
                } else {
                    grid[row][col] = shuffled.get(idx++);
                    collected[row][col] = false;
                }
            }
        }
    }

    /**
     * Check if a player has collected a specific item.
     * Returns true if a new cell was marked.
     */
    public boolean markItem(Item item) {
        boolean marked = false;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (!collected[row][col] && grid[row][col] == item) {
                    collected[row][col] = true;
                    marked = true;
                }
            }
        }
        return marked;
    }

    /**
     * Check if any row, column, or diagonal is complete (Bingo!).
     * Returns the winning line description or null if no bingo.
     */
    public WinResult checkWin() {
        // Check rows
        for (int row = 0; row < SIZE; row++) {
            boolean rowWin = true;
            for (int col = 0; col < SIZE; col++) {
                if (!collected[row][col]) { rowWin = false; break; }
            }
            if (rowWin) return new WinResult(WinType.ROW, row);
        }

        // Check columns
        for (int col = 0; col < SIZE; col++) {
            boolean colWin = true;
            for (int row = 0; row < SIZE; row++) {
                if (!collected[row][col]) { colWin = false; break; }
            }
            if (colWin) return new WinResult(WinType.COLUMN, col);
        }

        // Check diagonal top-left to bottom-right
        boolean diag1 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!collected[i][i]) { diag1 = false; break; }
        }
        if (diag1) return new WinResult(WinType.DIAGONAL, 0);

        // Check diagonal top-right to bottom-left
        boolean diag2 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!collected[i][SIZE - 1 - i]) { diag2 = false; break; }
        }
        if (diag2) return new WinResult(WinType.DIAGONAL, 1);

        return null;
    }

    public Item getItem(int row, int col) { return grid[row][col]; }
    public boolean isCollected(int row, int col) { return collected[row][col]; }
    public boolean isFreeCenter(int row, int col) { return row == FREE_CENTER && col == FREE_CENTER; }
    public UUID getPlayerUUID() { return playerUUID; }
    public Item[][] getGrid() { return grid; }
    public boolean[][] getCollected() { return collected; }

    // Serialize item identifier for network
    public static String itemToString(Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    public static Item itemFromString(String id) {
        return Registries.ITEM.get(new Identifier(id));
    }

    public enum WinType { ROW, COLUMN, DIAGONAL }

    public record WinResult(WinType type, int index) {}
}
