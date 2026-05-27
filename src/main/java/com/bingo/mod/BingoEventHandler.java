package com.bingo.mod;

import com.bingo.mod.game.BingoGameManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Monitors player inventories for new items to detect collection.
 * Uses a tick-based diff approach to catch ALL methods of obtaining items
 * (pickup, crafting, trading, chest, etc.)
 */
public class BingoEventHandler {

    // Previous inventory snapshot per player (item id -> count)
    private static final Map<UUID, Map<String, Integer>> previousInventories = new HashMap<>();

    public static void registerEvents() {

        // Every tick, diff inventories
        ServerTickEvents.END_SERVER_TICK.register(BingoEventHandler::onServerTick);

        // Clean up when player leaves
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            previousInventories.remove(handler.player.getUuid());
        });
    }

    private static void onServerTick(MinecraftServer server) {
        if (!BingoGameManager.getInstance().isGameActive()) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID uuid = player.getUuid();
            Map<String, Integer> current = getInventorySnapshot(player);
            Map<String, Integer> previous = previousInventories.getOrDefault(uuid, new HashMap<>());

            // Find newly acquired items
            for (Map.Entry<String, Integer> entry : current.entrySet()) {
                String itemId = entry.getKey();
                int currentCount = entry.getValue();
                int previousCount = previous.getOrDefault(itemId, 0);

                if (currentCount > previousCount) {
                    // Player gained this item! Check bingo card
                    net.minecraft.item.Item item = com.bingo.mod.game.BingoCard.itemFromString(itemId);
                    if (item != null && item != net.minecraft.item.Items.AIR) {
                        BingoGameManager.getInstance().onItemCollected(player, item);
                    }
                }
            }

            previousInventories.put(uuid, current);
        }
    }

    private static Map<String, Integer> getInventorySnapshot(ServerPlayerEntity player) {
        Map<String, Integer> snapshot = new HashMap<>();

        // Main inventory (36 slots) + armor (4) + offhand (1)
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty()) {
                String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                snapshot.merge(id, stack.getCount(), Integer::sum);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty()) {
                String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                snapshot.merge(id, stack.getCount(), Integer::sum);
            }
        }
        for (ItemStack stack : player.getInventory().offHand) {
            if (!stack.isEmpty()) {
                String id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
                snapshot.merge(id, stack.getCount(), Integer::sum);
            }
        }

        return snapshot;
    }
}
