package de.bingo;

import de.bingo.network.BingoNetworking;
import de.bingo.util.BingoGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingoMod implements ModInitializer {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static BingoGame currentGame = null;

    @Override
    public void onInitialize() {
        LOGGER.info("Minecraft Bingo Mod loaded!");
        registerPayloads();
        BingoNetworking.registerServerPackets();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("bingo")
                .executes(ctx -> {
                    BingoNetworking.sendOpenGuiPacket(ctx.getSource().getPlayer());
                    return 1;
                })
                .then(CommandManager.literal("start")
                    .executes(ctx -> {
                        currentGame = new BingoGame();
                        currentGame.generateBoard();
                        BingoNetworking.broadcastGameState(ctx.getSource().getServer());
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("§a§lBingo Spiel gestartet! /bingo zum Öffnen!"),
                            true
                        );
                        return 1;
                    })
                )
                .then(CommandManager.literal("stop")
                    .executes(ctx -> {
                        currentGame = null;
                        BingoNetworking.broadcastGameStop(ctx.getSource().getServer());
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("§c§lBingo Spiel beendet."),
                            true
                        );
                        return 1;
                    })
                )
            );
        });
    }

    private void registerPayloads() {
        registerPayload(BingoNetworking.OPEN_GUI_ID);
        registerPayload(BingoNetworking.SYNC_BOARD_ID);
        registerPayload(BingoNetworking.SYNC_PROGRESS_ID);
        registerPayload(BingoNetworking.BINGO_WIN_ID);
        registerPayload(BingoNetworking.GAME_STOP_ID);
    }

    private void registerPayload(Identifier id) {
        CustomPayload.Id<BingoNetworking.RawPacketPayload> payloadId =
            new CustomPayload.Id<>(id);
        PayloadTypeRegistry.playS2C().register(
            payloadId,
            PacketCodec.of(
                (value, buf) -> buf.writeBytes(value.buf().copy()),
                buf -> new BingoNetworking.RawPacketPayload(id, buf)
            )
        );
    }
}
