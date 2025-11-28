package io.github.tanguygab.betterstatisticsexpansion.listeners;

import io.github.tanguygab.betterstatisticsexpansion.BetterStatistics;
import io.github.tanguygab.betterstatisticsexpansion.BlocksData;
import io.github.tanguygab.betterstatisticsexpansion.StatListener;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener extends StatListener {

    private final NamespacedKey placedBlockKey;

    public BlockListener(BetterStatistics expansion) {
        super(expansion);
        placedBlockKey = new NamespacedKey(expansion.getPlaceholderAPI(), "placed");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlockPlaced();

        BlocksData.setPersistentData(block, placedBlockKey, PersistentDataType.BOOLEAN, true);

        expansion.incValue(player, "blocks-placed." + block.getType(), player.getWorld());
        expansion.incValue(player, "blocks-placed.*"                 , player.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        expansion.incValue(player, "blocks-broken." + block.getType(), player.getWorld());
        expansion.incValue(player, "blocks-broken.*"                 , player.getWorld());

        if (BlocksData.hasPersistentData(block, placedBlockKey)) {
            BlocksData.removePersistentData(block, placedBlockKey);
            return;
        }
        expansion.incValue(player, "generated-blocks-broken." + block.getType(), player.getWorld());
        expansion.incValue(player, "generated-blocks-broken.*"                 , player.getWorld());
    }


}
