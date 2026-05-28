package de.bingo.network;

import de.bingo.BingoMod;
import de.bingo.util.BingoGame;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class BingoNetworking {

    public static final Identifier OPEN_GUI_ID    = Identifier.of("bingo", "open_gui");
    public static final Identifier SYNC_BOARD_ID  = Identifier.of("bingo", "sync_board");
    public static final Identifier SYNC_PROGRESS_ID = Identifier.of("bingo", "sync_progress");
    public static final Identifier BINGO_WIN_ID   = Identifier.of("bingo", "bingo_win");
    public static final Identifier GAME_STOP_ID   = Identifier.of("bingo", "game_stop");

    private static PacketByteBuf newBuf() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void registerServerPackets() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (BingoMod.currentGame == null) return;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.getUuid();
                boolean[] progress = BingoMod.currentGame.getProgress(uuid);
                PlayerInventory inv = player.getInventory();
                Item[] board = BingoMod.currentGame.getBoard();
                boolean changed = false;

                for (int slot = 0; slot < inv.size(); slot++) {
                    Item item = inv.getStack(slot).getItem();
                    for (int i = 0; i < BingoGame.TOTAL; i++) {
                        if (board[i] == item && !progress[i]) {
                            progress[i] = true;
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    if (BingoMod.currentGame.checkBingo(progress) && !BingoMod.currentGame.hasWon(uuid)) {
                        BingoMod.currentGame.addWinner(uuid);
                        sendBingoWin(player, server);
                    }
                    sendProgressSync(player, progress);
                }
            }
        });
    }

    public static void sendOpenGuiPacket(ServerPlayerEntity player) {
        if (player == null) return;
        PacketByteBuf buf = newBuf();
        if (BingoMod.currentGame != null) {
            buf.writeBoolean(true);
            for (String id : BingoMod.currentGame.getBoardItemIds()) buf.writeString(id);
            for (boolean b : BingoMod.currentGame.getProgress(player.getUuid())) buf.writeBoolean(b);
        } else {
            buf.writeBoolean(false);
        }
        ServerPlayNetworking.send(player, new RawPacketPayload(OPEN_GUI_ID, buf));
    }

    public static void broadcastGameState(MinecraftServer server) {
        if (BingoMod.currentGame == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendBoardSync(player);
        }
    }

    public static void sendBoardSync(ServerPlayerEntity player) {
        if (BingoMod.currentGame == null) return;
        PacketByteBuf buf = newBuf();
        for (String id : BingoMod.currentGame.getBoardItemIds()) buf.writeString(id);
        for (boolean b : BingoMod.currentGame.getProgress(player.getUuid())) buf.writeBoolean(b);
        ServerPlayNetworking.send(player, new RawPacketPayload(SYNC_BOARD_ID, buf));
    }

    public static void sendProgressSync(ServerPlayerEntity player, boolean[] progress) {
        PacketByteBuf buf = newBuf();
        for (boolean b : progress) buf.writeBoolean(b);
        ServerPlayNetworking.send(player, new RawPacketPayload(SYNC_PROGRESS_ID, buf));
    }

    public static void sendBingoWin(ServerPlayerEntity winner, MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = newBuf();
            buf.writeString(winner.getName().getString());
            ServerPlayNetworking.send(player, new RawPacketPayload(BINGO_WIN_ID, buf));
        }
        server.getPlayerManager().broadcast(
            net.minecraft.text.Text.literal("§6§l🎉 BINGO! §e" + winner.getName().getString() + " §6hat gewonnen! 🎉"),
            false
        );
    }

    public static void broadcastGameStop(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PacketByteBuf buf = newBuf();
            ServerPlayNetworking.send(player, new RawPacketPayload(GAME_STOP_ID, buf));
        }
    }

    public record RawPacketPayload(Identifier id, PacketByteBuf buf) implements CustomPayload {
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return new CustomPayload.Id<>(id);
        }
    }
}
