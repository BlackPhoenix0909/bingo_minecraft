package com.bingo.mod.command;

import com.bingo.mod.game.BingoGameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BingoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("bingo")
                .executes(BingoCommand::openCard)          // /bingo -> open card
                .then(CommandManager.literal("start")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(BingoCommand::startGame))    // /bingo start (op only)
                .then(CommandManager.literal("stop")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(BingoCommand::stopGame))     // /bingo stop (op only)
                .then(CommandManager.literal("open")
                    .executes(BingoCommand::openCard))     // /bingo open -> open card
        );
    }

    private static int startGame(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        BingoGameManager manager = BingoGameManager.getInstance();

        if (manager.isGameActive()) {
            source.sendMessage(Text.literal("⚠ Ein Bingo-Spiel läuft bereits!")
                .formatted(Formatting.RED));
            return 0;
        }

        boolean started = manager.startGame(source.getServer());
        if (started) {
            source.sendMessage(Text.literal("✔ Bingo-Spiel gestartet!")
                .formatted(Formatting.GREEN));
        }
        return 1;
    }

    private static int stopGame(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        BingoGameManager manager = BingoGameManager.getInstance();

        if (!manager.isGameActive()) {
            source.sendMessage(Text.literal("⚠ Kein aktives Bingo-Spiel!")
                .formatted(Formatting.RED));
            return 0;
        }

        manager.stopGame();
        source.sendMessage(Text.literal("✔ Bingo-Spiel gestoppt!").formatted(Formatting.YELLOW));
        return 1;
    }

    private static int openCard(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        BingoGameManager manager = BingoGameManager.getInstance();

        if (!manager.isGameActive()) {
            source.sendMessage(Text.literal("⚠ Kein aktives Bingo-Spiel! Nutze /bingo start")
                .formatted(Formatting.RED));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendMessage(Text.literal("Dieser Befehl ist nur für Spieler!")
                .formatted(Formatting.RED));
            return 0;
        }

        manager.openCardForPlayer(player);
        return 1;
    }
}
