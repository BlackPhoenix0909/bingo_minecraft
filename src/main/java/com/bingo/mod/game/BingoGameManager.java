package com.bingo.mod.game;

import com.bingo.mod.BingoMod;
import com.bingo.mod.network.BingoNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side singleton managing the current bingo game state.
 */
public class BingoGameManager {

    private static BingoGameManager instance;
    private boolean gameActive = false;
    private final Map<UUID, BingoCard> playerCards = new HashMap<>();
    private MinecraftServer server;

    private BingoGameManager() {}

    public static BingoGameManager getInstance() {
        if (instance == null) instance = new BingoGameManager();
        return instance;
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    public boolean isGameActive() { return gameActive; }

    /**
     * Start a new bingo game. Generates cards for all online players.
     */
    public boolean startGame(MinecraftServer server) {
        if (gameActive) return false;

        this.server = server;
        gameActive = true;
        playerCards.clear();

        // Generate a card for every online player
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            BingoCard card = new BingoCard(player.getUuid());
            playerCards.put(player.getUuid(), card);

            // Send card to client
            BingoNetworking.sendCardToPlayer(player, card);

            // Play start sound
            player.playSound(BingoMod.GAME_START_SOUND, SoundCategory.MASTER, 1.0f, 1.0f);
        }

        // Broadcast start message
        server.getPlayerManager().broadcast(
            Text.literal("╔══════════════════╗").formatted(Formatting.GOLD), false);
        server.getPlayerManager().broadcast(
            Text.literal("  🎯 BINGO startet! 🎯  ").formatted(Formatting.YELLOW), false);
        server.getPlayerManager().broadcast(
            Text.literal("  Nutze /bingo für deine Karte!").formatted(Formatting.WHITE), false);
        server.getPlayerManager().broadcast(
            Text.literal("╚══════════════════╝").formatted(Formatting.GOLD), false);

        return true;
    }

    /**
     * Stop the current game.
     */
    public boolean stopGame() {
        if (!gameActive) return false;
        gameActive = false;
        playerCards.clear();

        if (server != null) {
            server.getPlayerManager().broadcast(
                Text.literal("Bingo-Spiel beendet!").formatted(Formatting.RED), false);
        }
        return true;
    }

    /**
     * Called when a player picks up an item. Checks and updates their card.
     */
    public void onItemCollected(ServerPlayerEntity player, net.minecraft.item.Item item) {
        if (!gameActive) return;

        BingoCard card = playerCards.get(player.getUuid());
        if (card == null) {
            // Late joiner - generate a card
            card = new BingoCard(player.getUuid());
            playerCards.put(player.getUuid(), card);
        }

        boolean marked = card.markItem(item);
        if (!marked) return;

        // Send updated card to player
        BingoNetworking.sendCardToPlayer(player, card);
        BingoNetworking.sendItemCollected(player, item);

        // Play collect sound
        player.playSound(BingoMod.ITEM_COLLECT_SOUND, SoundCategory.MASTER, 0.8f, 1.2f);

        // Check for win
        BingoCard.WinResult win = card.checkWin();
        if (win != null) {
            handleWin(player, win);
        }
    }

    private void handleWin(ServerPlayerEntity winner, BingoCard.WinResult win) {
        String winTypeText = switch (win.type()) {
            case ROW -> "Reihe " + (win.index() + 1);
            case COLUMN -> "Spalte " + (win.index() + 1);
            case DIAGONAL -> "Diagonal";
        };

        // Broadcast win to all players
        if (server != null) {
            server.getPlayerManager().broadcast(
                Text.literal(""), false);
            server.getPlayerManager().broadcast(
                Text.literal("★══════════════════════★").formatted(Formatting.GOLD), false);
            server.getPlayerManager().broadcast(
                Text.literal("  🎉 BINGO! 🎉").formatted(Formatting.YELLOW), false);
            server.getPlayerManager().broadcast(
                Text.literal("  " + winner.getName().getString() + " hat gewonnen!")
                    .formatted(Formatting.GREEN), false);
            server.getPlayerManager().broadcast(
                Text.literal("  (" + winTypeText + ")").formatted(Formatting.AQUA), false);
            server.getPlayerManager().broadcast(
                Text.literal("★══════════════════════★").formatted(Formatting.GOLD), false);

            // Play win sound for all
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.playSound(BingoMod.BINGO_WIN_SOUND, SoundCategory.MASTER, 1.0f, 1.0f);
            }

            // Notify winner client for special screen
            BingoNetworking.sendWinNotification(winner);
        }

        gameActive = false;
    }

    public BingoCard getCard(UUID playerUUID) {
        return playerCards.get(playerUUID);
    }

    public void openCardForPlayer(ServerPlayerEntity player) {
        BingoCard card = playerCards.get(player.getUuid());
        if (card != null) {
            BingoNetworking.sendOpenGui(player, card);
        }
    }
}
