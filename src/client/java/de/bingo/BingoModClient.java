package de.bingo;

import de.bingo.network.BingoNetworkingClient;
import net.fabricmc.api.ClientModInitializer;

public class BingoModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BingoNetworkingClient.registerClientPackets();
        BingoMod.LOGGER.info("Bingo client initialized!");
    }
}
