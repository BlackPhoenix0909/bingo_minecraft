package de.bingo.util;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

public class BingoGame {

    public static final int SIZE = 3; // 3x3 Bingo board
    public static final int TOTAL = SIZE * SIZE;

    // All possible items that can appear on the bingo board
    private static final Item[] BINGO_ITEMS = {
        Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT, Items.EMERALD,
        Items.COAL, Items.REDSTONE, Items.LAPIS_LAZULI, Items.QUARTZ,
        Items.OBSIDIAN, Items.BLAZE_ROD, Items.ENDER_PEARL, Items.SLIME_BALL,
        Items.SPIDER_EYE, Items.STRING, Items.FEATHER, Items.BONE,
        Items.GUNPOWDER, Items.SUGAR, Items.INK_SAC, Items.GLOWSTONE_DUST,
        Items.LEATHER, Items.RABBIT_HIDE, Items.WOOL,
        Items.OAK_LOG, Items.SAND, Items.GRAVEL, Items.CLAY_BALL,
        Items.FLINT, Items.PAPER, Items.BOOK,
        Items.BREAD, Items.APPLE, Items.CARROT, Items.POTATO,
        Items.EGG, Items.MILK_BUCKET, Items.PUMPKIN, Items.MELON_SLICE,
        Items.CACTUS, Items.BAMBOO, Items.VINE, Items.LILY_PAD,
        Items.IRON_SWORD, Items.BOW, Items.ARROW, Items.SHIELD,
        Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL,
        Items.CRAFTING_TABLE, Items.FURNACE, Items.CHEST,
        Items.TORCH, Items.LADDER, Items.SIGN, Items.BUCKET,
        Items.FISHING_ROD, Items.CLOCK, Items.COMPASS, Items.MAP
    };

    // The 9 items on the board
    private final Item[] board = new Item[TOTAL];
    // Which cells are checked (collected) per player UUID
    private final Map<UUID, boolean[]> playerProgress = new HashMap<>();
    // Which players have won
    private final Set<UUID> winners = new HashSet<>();

    public void generateBoard() {
        List<Item> pool = new ArrayList<>(Arrays.asList(BINGO_ITEMS));
        Collections.shuffle(pool);
        for (int i = 0; i < TOTAL; i++) {
            board[i] = pool.get(i);
        }
    }

    public Item[] getBoard() {
        return board;
    }

    public boolean[] getProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, k -> new boolean[TOTAL]);
    }

    /**
     * Called when a player collects an item. Returns true if this triggers a bingo.
     */
    public boolean collectItem(UUID playerId, Item item) {
        boolean[] progress = getProgress(playerId);
        boolean changed = false;
        for (int i = 0; i < TOTAL; i++) {
            if (board[i] == item && !progress[i]) {
                progress[i] = true;
                changed = true;
            }
        }
        if (changed && checkBingo(progress)) {
            winners.add(playerId);
            return true;
        }
        return false;
    }

    /**
     * Check if the given progress has 3-in-a-row (bingo).
     * For a 3x3 grid: rows, columns, diagonals.
     */
    public boolean checkBingo(boolean[] progress) {
        // Rows
        for (int r = 0; r < SIZE; r++) {
            boolean row = true;
            for (int c = 0; c < SIZE; c++) {
                if (!progress[r * SIZE + c]) { row = false; break; }
            }
            if (row) return true;
        }
        // Columns
        for (int c = 0; c < SIZE; c++) {
            boolean col = true;
            for (int r = 0; r < SIZE; r++) {
                if (!progress[r * SIZE + c]) { col = false; break; }
            }
            if (col) return true;
        }
        // Diagonal top-left to bottom-right
        boolean diag1 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!progress[i * SIZE + i]) { diag1 = false; break; }
        }
        if (diag1) return true;
        // Diagonal top-right to bottom-left
        boolean diag2 = true;
        for (int i = 0; i < SIZE; i++) {
            if (!progress[i * SIZE + (SIZE - 1 - i)]) { diag2 = false; break; }
        }
        return diag2;
    }

    public boolean hasWon(UUID playerId) {
        return winners.contains(playerId);
    }

    /**
     * Get item identifiers as strings for network sync
     */
    public String[] getBoardItemIds() {
        String[] ids = new String[TOTAL];
        for (int i = 0; i < TOTAL; i++) {
            ids[i] = Registries.ITEM.getId(board[i]).toString();
        }
        return ids;
    }
}
