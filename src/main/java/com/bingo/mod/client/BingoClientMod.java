package com.bingo.mod.client;

import com.bingo.mod.game.BingoCard;
import com.bingo.mod.network.BingoNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;

/**
 * Client-side mod initializer: registers packet handlers and stores client bingo state.
 */
public class BingoClientMod implements ClientModInitializer {

    // Client-side card state
    private static Item[][] clientGrid = new Item[BingoCard.SIZE][BingoCard.SIZE];
    private static boolean[][] clientCollected = new boolean[BingoCard.SIZE][BingoCard.SIZE];
    private static boolean hasCard = false;
    private static boolean showingWin = false;

    @Override
    public void onInitializeClient() {

        // Receive card sync (passive update)
        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.CARD_SYNC_PACKET, (client, handler, buf, responseSender) -> {
            readCardFromBuf(buf);
            // Update existing screen if open
            client.execute(() -> {
                if (client.currentScreen instanceof BingoScreen screen) {
                    screen.refreshCard();
                }
            });
        });

        // Receive open GUI command from server
        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.OPEN_GUI_PACKET, (client, handler, buf, responseSender) -> {
            readCardFromBuf(buf);
            client.execute(() -> {
                client.setScreen(new BingoScreen());
            });
        });

        // Receive item collected notification (could play client-side animation)
        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.ITEM_COLLECTED_PACKET, (client, handler, buf, responseSender) -> {
            String itemId = buf.readString();
            client.execute(() -> {
                // The card is already updated via CARD_SYNC, this is for extra feedback
                if (client.currentScreen instanceof BingoScreen screen) {
                    screen.playCollectAnimation(itemId);
                }
            });
        });

        // Receive win notification
        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.WIN_PACKET, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                showingWin = true;
                client.setScreen(new BingoWinScreen());
            });
        });
    }

    private static void readCardFromBuf(net.minecraft.network.PacketByteBuf buf) {
        for (int row = 0; row < BingoCard.SIZE; row++) {
            for (int col = 0; col < BingoCard.SIZE; col++) {
                String itemId = buf.readString();
                boolean collected = buf.readBoolean();
                clientGrid[row][col] = BingoCard.itemFromString(itemId);
                clientCollected[row][col] = collected;
            }
        }
        hasCard = true;
    }

    public static Item[][] getClientGrid() { return clientGrid; }
    public static boolean[][] getClientCollected() { return clientCollected; }
    public static boolean hasCard() { return hasCard; }
}
