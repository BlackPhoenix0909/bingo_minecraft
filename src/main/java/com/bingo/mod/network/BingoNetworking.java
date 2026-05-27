package com.bingo.mod.network;

import com.bingo.mod.BingoMod;
import com.bingo.mod.game.BingoCard;
import com.bingo.mod.game.BingoGameManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * All network packets for the Bingo mod.
 * S2C = Server to Client
 * C2S = Client to Server
 */
public class BingoNetworking {

    // Server -> Client: Send full bingo card
    public static final Identifier CARD_SYNC_PACKET = new Identifier(BingoMod.MOD_ID, "card_sync");

    // Server -> Client: Open the bingo GUI
    public static final Identifier OPEN_GUI_PACKET = new Identifier(BingoMod.MOD_ID, "open_gui");

    // Server -> Client: Notify that an item was collected
    public static final Identifier ITEM_COLLECTED_PACKET = new Identifier(BingoMod.MOD_ID, "item_collected");

    // Server -> Client: Win notification
    public static final Identifier WIN_PACKET = new Identifier(BingoMod.MOD_ID, "win");

    // Client -> Server: Request open GUI (already handled by command, but backup)
    public static final Identifier REQUEST_OPEN_PACKET = new Identifier(BingoMod.MOD_ID, "request_open");

    public static void registerServerPackets() {
        // Handle client requesting to open the GUI
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_OPEN_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                BingoGameManager.getInstance().openCardForPlayer(player);
            });
        });
    }

    /**
     * Send the full bingo card to a player (serialized as flat arrays).
     */
    public static void sendCardToPlayer(ServerPlayerEntity player, BingoCard card) {
        PacketByteBuf buf = PacketByteBufs.create();

        // Write 5x5 grid of item ids + collected status
        for (int row = 0; row < BingoCard.SIZE; row++) {
            for (int col = 0; col < BingoCard.SIZE; col++) {
                buf.writeString(BingoCard.itemToString(card.getItem(row, col)));
                buf.writeBoolean(card.isCollected(row, col));
            }
        }

        ServerPlayNetworking.send(player, CARD_SYNC_PACKET, buf);
    }

    /**
     * Tell the client to open the bingo GUI (also sends card data).
     */
    public static void sendOpenGui(ServerPlayerEntity player, BingoCard card) {
        PacketByteBuf buf = PacketByteBufs.create();

        for (int row = 0; row < BingoCard.SIZE; row++) {
            for (int col = 0; col < BingoCard.SIZE; col++) {
                buf.writeString(BingoCard.itemToString(card.getItem(row, col)));
                buf.writeBoolean(card.isCollected(row, col));
            }
        }

        ServerPlayNetworking.send(player, OPEN_GUI_PACKET, buf);
    }

    /**
     * Notify client that a specific item was collected (for visual feedback).
     */
    public static void sendItemCollected(ServerPlayerEntity player, Item item) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(BingoCard.itemToString(item));
        ServerPlayNetworking.send(player, ITEM_COLLECTED_PACKET, buf);
    }

    /**
     * Notify the winner's client to show the win screen.
     */
    public static void sendWinNotification(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.empty();
        ServerPlayNetworking.send(player, WIN_PACKET, buf);
    }
}
