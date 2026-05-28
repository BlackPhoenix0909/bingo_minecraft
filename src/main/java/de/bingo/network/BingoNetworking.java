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

    public record BoardPayload(boolean active, List<String> itemIds, List<Boolean> progress)
            implements CustomPayload {
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
        PayloadTypeRegistry.playS2C().register(BoardPayload.ID,    BoardPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ProgressPayload.ID, ProgressPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WinPayload.ID,      WinPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(StopPayload.ID,     StopPayload.CODEC);
    }

    public static void registerServerPackets() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (BingoMod.currentGame == null) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID uuid = player.g
