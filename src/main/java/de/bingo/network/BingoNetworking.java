package de.bingo.network;

import de.bingo.BingoMod;
import de.bingo.util.BingoGame;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BingoNetworking {

    public record BoardPayload(boolean active, List<String> itemIds, List<Boolean> progress) implements CustomPayload {
        public static final Id<BoardPayload> ID = new Id<>(Identifier.of("bingo", "board"));
        public static final PacketCodec<RegistryByteBuf, BoardPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, BoardPayload::active,
            PacketCodecs.STRING.collect(PacketCodecs.toList()), BoardPayload::itemIds,
            PacketCodecs.BOOL.collect(PacketCodecs.toList()), BoardPayload::progress,
            BoardPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ProgressPayload(List<Boolean> progress) implements CustomPayload {
        public static final Id<ProgressPayload> ID = new Id<>(Identifier.of("bingo", "progress"));
        public static final PacketCodec<RegistryByteBuf, ProgressPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL.collect(PacketCodecs.toList()), ProgressPayload::progress,
            ProgressPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record WinPayload(String winner) implements CustomPayload {
        public static final Id<WinPayload> ID = new Id<>(Identifier.of("bingo", "win"));
        public static final PacketCodec<RegistryByteBuf, WinPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, WinPayload::winner,
            WinPayload::new);
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public record StopPayload() implements CustomPayload {
        public static final Id<StopPayload> ID = new Id<>(Identifier.of("bingo", "stop"));
        public static final PacketCodec<RegistryByteBuf, StopPayload> CODEC = PacketCodec.unit(new StopPayload());
        @Override public Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.playS2C().register(BoardPayload.ID, BoardPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ProgressPayload.ID, ProgressPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WinPayload.ID, WinPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopPayload.ID, StopPayload.CODEC);
    }

    public static void registerServerPackets() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
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
        if (BingoMod.currentGame != null) {
            ServerPlayNetworking.send(player, new BoardPayload(
                true,
                List.of(BingoMod.currentGame.getBoardItemIds()),
                boolArrayToList(BingoMod.currentGame.getProgress(player.getUuid()))));
        } else {
            ServerPlayNetworking.send(player, new BoardPayload(false, List.of(), List.of()));
        }
    }

    public static void broadcastGameState(MinecraftServer server) {
        if (BingoMod.currentGame == null) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendOpenGuiPacket(player);
        }
    }

    public static void sendProgressSync(ServerPlayerEntity player, boolean[] progress) {
        ServerPlayNetworking.send(player, new ProgressPayload(boolArrayToList(progress)));
    }

    public static void sendBingoWin(ServerPlayerEntity winner, MinecraftServer server) {
        String name = winner.getName().getString();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new WinPayload(name));
        }
        server.getPlayerManager().broadcast(
            Text.literal("§6§l🎉 BINGO! §e" + name + " §6hat gewonnen! 🎉"), false);
    }

    public static void broadcastGameStop(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, new StopPayload());
        }
    }

    private static List<Boolean> boolArrayToList(boolean[] arr) {
        List<Boolean> list = new ArrayList<>(arr.length);
        for (boolean b : arr) list.add(b);
        return list;
    }
}
