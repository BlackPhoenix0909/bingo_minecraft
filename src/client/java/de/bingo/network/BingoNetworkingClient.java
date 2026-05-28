package de.bingo.network;

import de.bingo.screen.BingoScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class BingoNetworkingClient {

    public static Item[] clientBoard = null;
    public static boolean[] clientProgress = null;
    public static boolean gameActive = false;

    public static void registerClientPackets() {

        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.BoardPayload.ID, (payload, context) -> {
            if (!payload.active()) {
                context.client().execute(() -> {
                    gameActive = false;
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("§cKein Bingo-Spiel aktiv! Admin muss /bingo start ausführen."), false);
                    }
                });
                return;
            }
            Item[] board = readBoard(payload.itemIds());
            boolean[] progress = readProgress(payload.progress());
            context.client().execute(() -> {
                clientBoard = board;
                clientProgress = progress;
                gameActive = true;
                context.client().setScreen(new BingoScreen(board, progress));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.ProgressPayload.ID, (payload, context) -> {
            boolean[] progress = readProgress(payload.progress());
            context.client().execute(() -> {
                clientProgress = progress;
                if (context.client().currentScreen instanceof BingoScreen screen) {
                    screen.refreshProgress(progress);
                }
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.playSound(
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.WinPayload.ID, (payload, context) -> {
            String winner = payload.winner();
            context.client().execute(() -> {
                gameActive = false;
                if (MinecraftClient.getInstance().player != null) {
                    String local = MinecraftClient.getInstance().player.getName().getString();
                    if (winner.equals(local)) {
                        MinecraftClient.getInstance().player.playSound(
                            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                    } else {
                        MinecraftClient.getInstance().player.playSound(
                            SoundEvents.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
                    }
                }
                if (context.client().currentScreen instanceof BingoScreen screen) {
                    screen.showWinner(winner);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(BingoNetworking.StopPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                gameActive = false;
                clientBoard = null;
                clientProgress = null;
                if (context.client().currentScreen instanceof BingoScreen) {
                    context.client().setScreen(null);
                }
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§c§lBingo-Spiel beendet!"), false);
                }
            });
        });
    }

    private static Item[] readBoard(List<String> ids) {
        Item[] board = new Item[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            board[i] = Registries.ITEM.get(Identifier.tryParse(ids.get(i)));
        }
        return board;
    }

    private static boolean[] readProgress(List<Boolean> list) {
        boolean[] arr = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
