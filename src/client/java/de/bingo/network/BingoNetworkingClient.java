package de.bingo.network;

import de.bingo.screen.BingoScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BingoNetworkingClient {

    // Client-side state
    public static Item[] clientBoard = null;
    public static boolean[] clientProgress = null;
    public static boolean gameActive = false;
    public static String lastWinner = null;

    public static void registerClientPackets() {

        // Open GUI packet
        ClientPlayNetworking.registerGlobalReceiver(
            new net.minecraft.network.packet.CustomPayload.Id<>(BingoNetworking.OPEN_GUI_ID),
            (payload, context) -> {
                var buf = ((BingoNetworking.RawPacketPayload) payload).buf();
                boolean active = buf.readBoolean();
                if (active) {
                    Item[] board = readBoard(buf);
                    boolean[] progress = readProgress(buf);
                    context.client().execute(() -> {
                        clientBoard = board;
                        clientProgress = progress;
                        gameActive = true;
                        context.client().setScreen(new BingoScreen(board, progress));
                    });
                } else {
                    context.client().execute(() -> {
                        gameActive = false;
                        MinecraftClient.getInstance().player.sendMessage(
                            net.minecraft.text.Text.literal("§cKein Bingo-Spiel aktiv! Admin muss §l/bingo start§r§c ausführen."),
                            false
                        );
                    });
                }
            }
        );

        // Sync board packet (broadcast on game start)
        ClientPlayNetworking.registerGlobalReceiver(
            new net.minecraft.network.packet.CustomPayload.Id<>(BingoNetworking.SYNC_BOARD_ID),
            (payload, context) -> {
                var buf = ((BingoNetworking.RawPacketPayload) payload).buf();
                Item[] board = readBoard(buf);
                boolean[] progress = readProgress(buf);
                context.client().execute(() -> {
                    clientBoard = board;
                    clientProgress = progress;
                    gameActive = true;
                    // If bingo screen is open, refresh it
                    if (context.client().currentScreen instanceof BingoScreen screen) {
                        screen.refreshState(board, progress);
                    }
                    // Notify player
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                            net.minecraft.text.Text.literal("§a§lNeues Bingo-Spiel gestartet! Tippe §e/bingo§a§l zum Öffnen!"),
                            false
                        );
                    }
                });
            }
        );

        // Progress sync packet
        ClientPlayNetworking.registerGlobalReceiver(
            new net.minecraft.network.packet.CustomPayload.Id<>(BingoNetworking.SYNC_PROGRESS_ID),
            (payload, context) -> {
                var buf = ((BingoNetworking.RawPacketPayload) payload).buf();
                boolean[] progress = readProgress(buf);
                context.client().execute(() -> {
                    clientProgress = progress;
                    if (context.client().currentScreen instanceof BingoScreen screen) {
                        screen.refreshProgress(progress);
                    }
                    // Play ding sound when item collected
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.playSound(
                            net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                            1.0f, 1.2f
                        );
                    }
                });
            }
        );

        // Bingo win packet
        ClientPlayNetworking.registerGlobalReceiver(
            new net.minecraft.network.packet.CustomPayload.Id<>(BingoNetworking.BINGO_WIN_ID),
            (payload, context) -> {
                var buf = ((BingoNetworking.RawPacketPayload) payload).buf();
                String winner = buf.readString();
                context.client().execute(() -> {
                    lastWinner = winner;
                    gameActive = false;
                    // Play win sound
                    if (MinecraftClient.getInstance().player != null) {
                        String localName = MinecraftClient.getInstance().player.getName().getString();
                        if (winner.equals(localName)) {
                            // You won!
                            MinecraftClient.getInstance().player.playSound(
                                net.minecraft.sound.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                                1.0f, 1.0f
                            );
                        } else {
                            MinecraftClient.getInstance().player.playSound(
                                net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_NO,
                                1.0f, 0.8f
                            );
                        }
                    }
                    // Show win screen if bingo screen is open
                    if (context.client().currentScreen instanceof BingoScreen screen) {
                        screen.showWinner(winner);
                    }
                });
            }
        );

        // Game stop packet
        ClientPlayNetworking.registerGlobalReceiver(
            new net.minecraft.network.packet.CustomPayload.Id<>(BingoNetworking.GAME_STOP_ID),
            (payload, context) -> {
                context.client().execute(() -> {
                    gameActive = false;
                    clientBoard = null;
                    clientProgress = null;
                    if (context.client().currentScreen instanceof BingoScreen) {
                        context.client().setScreen(null);
                    }
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                            net.minecraft.text.Text.literal("§c§lBingo-Spiel beendet!"),
                            false
                        );
                    }
                });
            }
        );
    }

    private static Item[] readBoard(net.minecraft.network.PacketByteBuf buf) {
        int size = de.bingo.util.BingoGame.SIZE * de.bingo.util.BingoGame.SIZE;
        Item[] board = new Item[size];
        for (int i = 0; i < size; i++) {
            String id = buf.readString();
            board[i] = Registries.ITEM.get(Identifier.tryParse(id));
        }
        return board;
    }

    private static boolean[] readProgress(net.minecraft.network.PacketByteBuf buf) {
        int size = de.bingo.util.BingoGame.SIZE * de.bingo.util.BingoGame.SIZE;
        boolean[] progress = new boolean[size];
        for (int i = 0; i < size; i++) {
            progress[i] = buf.readBoolean();
        }
        return progress;
    }
}
