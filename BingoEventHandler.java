package de.bingo;

import de.bingo.network.BingoNetworking;
import de.bingo.util.BingoGame;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingoMod implements ModInitializer {
    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Shared game state (server-side)
    public static BingoGame currentGame = null;

    @Override
    public void onInitialize() {
        LOGGER.info("Minecraft Bingo Mod loaded!");

        BingoNetworking.registerServerPackets();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("bingo")
                .executes(ctx -> {
                    // Open bingo GUI for the player
                    BingoNetworking.sendOpenGuiPacket(ctx.getSource().getPlayer());
                    return 1;
                })
                .then(CommandManager.literal("start")
                    .executes(ctx -> {
                        currentGame = new BingoGame();
                        currentGame.generateBoard();
                        // Broadcast the new game to all players
                        BingoNetworking.broadcastGameState(ctx.getSource().getServer());
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("§a§lBingo game started! Use /bingo to open your card."),
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
                            () -> Text.literal("§c§lBingo game stopped."),
                            true
                        );
                        return 1;
                    })
                )
            );
        });
    }
}
