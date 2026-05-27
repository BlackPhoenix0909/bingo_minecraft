package com.bingo.mod;

import com.bingo.mod.command.BingoCommand;
import com.bingo.mod.game.BingoGameManager;
import com.bingo.mod.network.BingoNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BingoMod implements ModInitializer {

    public static final String MOD_ID = "bingo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Sound Events
    public static final Identifier BINGO_WIN_ID = new Identifier(MOD_ID, "bingo_win");
    public static final Identifier ITEM_COLLECT_ID = new Identifier(MOD_ID, "item_collect");
    public static final Identifier GAME_START_ID = new Identifier(MOD_ID, "game_start");

    public static SoundEvent BINGO_WIN_SOUND;
    public static SoundEvent ITEM_COLLECT_SOUND;
    public static SoundEvent GAME_START_SOUND;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Minecraft Bingo Mod!");

        // Register sounds
        BINGO_WIN_SOUND = Registry.register(Registries.SOUND_EVENT,
                BINGO_WIN_ID, SoundEvent.of(BINGO_WIN_ID));
        ITEM_COLLECT_SOUND = Registry.register(Registries.SOUND_EVENT,
                ITEM_COLLECT_ID, SoundEvent.of(ITEM_COLLECT_ID));
        GAME_START_SOUND = Registry.register(Registries.SOUND_EVENT,
                GAME_START_ID, SoundEvent.of(GAME_START_ID));

        // Register networking
        BingoNetworking.registerServerPackets();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                BingoCommand.register(dispatcher));

        // Listen for inventory changes (item collection)
        BingoEventHandler.registerEvents();

        LOGGER.info("Bingo Mod initialized!");
    }
}
