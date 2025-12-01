package io.github.tanguygab.betterstatisticsexpansion.listeners;

import io.github.tanguygab.betterstatisticsexpansion.BetterStatistics;
import io.github.tanguygab.betterstatisticsexpansion.StatListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class PlayerListener extends StatListener {

    public PlayerListener(BetterStatistics expansion) {
        super(expansion);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        expansion.incValue(player, "deaths", player.getWorld());

        if (player.getKiller() == null) return;
        expansion.incValue(player.getKiller(), "kills", player.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreed(EntityBreedEvent e) {
        if (!(e.getBreeder() instanceof Player player)) return;

        expansion.incValue(player, "bred."+e.getMother().getType(), player.getWorld());
        expansion.incValue(player, "bred.*",                        player.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemSmelt(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack item = e.getInventory().getItem(2);
        if (item == null) return;

        int amount = 0;
        if (e.getSlotType() == InventoryType.SlotType.RESULT) {
            amount = switch (e.getAction()) {
                case NOTHING, CLONE_STACK -> 0;
                case DROP_ONE_SLOT -> 1;
                case MOVE_TO_OTHER_INVENTORY -> {
                    if (player.getInventory().firstEmpty() != -1) yield item.getAmount();

                    if (player.getInventory().contains(item.getType())) {
                        int space = Arrays.stream(player.getInventory().getStorageContents())
                                .filter(item::isSimilar)
                                .mapToInt(stack -> stack.getMaxStackSize() - stack.getAmount())
                                .sum();
                        yield Math.min(space, item.getAmount());
                    }
                    yield 0;
                }
                case PICKUP_HALF -> Math.ceilDiv(item.getAmount(), 2);
                default -> item.getAmount();
            };
        } else {
            if (e.getCursor() != null && item.isSimilar(e.getCursor()) && e.getCursor().getAmount() + item.getAmount() < e.getCursor().getMaxStackSize())
                amount = item.getAmount();
        }
        expansion.incValue(player, "smelt." + item.getType(), player.getWorld(), amount);
        expansion.incValue(player, "smelt.*"                , player.getWorld(), amount);
    }

}
