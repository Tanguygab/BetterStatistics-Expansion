package io.github.tanguygab.betterstatisticsexpansion.listeners;

import io.github.tanguygab.betterstatisticsexpansion.BetterStatistics;
import io.github.tanguygab.betterstatisticsexpansion.StatListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
        if (!(e.getWhoClicked() instanceof Player player) || e.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack item = e.getCurrentItem();
        ItemStack cursor = player.getItemOnCursor();
        if (item == null || item.getType() == Material.AIR || !item.isSimilar(cursor)) return;

        int amount = item.getAmount();
        if (cursor.getMaxStackSize() - cursor.getAmount() < amount) return;

        expansion.incValue(player, "smelt." + item.getType(), player.getWorld(), amount);
        expansion.incValue(player, "smelt.*"                , player.getWorld(), amount);
    }

}
