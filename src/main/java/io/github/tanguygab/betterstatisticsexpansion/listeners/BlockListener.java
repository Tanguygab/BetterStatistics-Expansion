package io.github.tanguygab.betterstatisticsexpansion.listeners;

import io.github.tanguygab.betterstatisticsexpansion.BetterStatistics;
import io.github.tanguygab.betterstatisticsexpansion.BlocksData;
import io.github.tanguygab.betterstatisticsexpansion.StatListener;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

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

        BlocksData.set(block, placedBlockKey, PersistentDataType.BOOLEAN, true);

        expansion.incValue(player, "blocks-placed." + block.getType(), player.getWorld());
        expansion.incValue(player, "blocks-placed.*"                 , player.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        boolean crop = block.getBlockData() instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge();
        if (crop) expansion.incValue(player, "blocks-broken.GROWN_" + block.getType(), player.getWorld());
        expansion.incValue(player, "blocks-broken." + block.getType(), player.getWorld());
        expansion.incValue(player, "blocks-broken.*"                 , player.getWorld());

        if (BlocksData.has(block, placedBlockKey)) {
            BlocksData.remove(block, placedBlockKey);
            return;
        }

        if (crop) expansion.incValue(player, "generated-blocks-broken.GROWN_" + block.getType(), player.getWorld());
        expansion.incValue(player, "generated-blocks-broken." + block.getType(), player.getWorld());
        expansion.incValue(player, "generated-blocks-broken.*"                 , player.getWorld());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDrop(BlockDropItemEvent e) {
        BlockState block = e.getBlockState();
        System.out.println("hi " + block.getType());
        if (!(block.getBlockData() instanceof Ageable ageable) || ageable.getAge() != ageable.getMaximumAge()) return;

        int amount = e.getItems()
                .stream()
                .map(Item::getItemStack)
                .filter(item -> block.getType().toString().matches(item.getType() + "(E?S)?"))
                .mapToInt(ItemStack::getAmount)
                .sum();
        System.out.println(amount);
        Player player = e.getPlayer();
        expansion.incValue(player, "crops-broken." + block.getType(), player.getWorld(), amount);
        expansion.incValue(player, "crops-broken.*"                 , player.getWorld(), amount);

        if (BlocksData.has(e.getBlock(), placedBlockKey)) {
            BlocksData.remove(e.getBlock(), placedBlockKey);
            return;
        }
        expansion.incValue(player, "generated-crops-broken." + block.getType(), player.getWorld(), amount);
        expansion.incValue(player, "generated-crops-broken.*"                 , player.getWorld(), amount);
    }

    private void onRemovedBlock(Block block) {
        if (BlocksData.has(block, placedBlockKey))
            BlocksData.remove(block, placedBlockKey);
    }

    private void onRemovedBlocks(List<Block> blocks) {
        blocks.forEach(this::onRemovedBlock);
    }

    private void onMovedBlock(Block oldBlock, Block newBlock) {
        BlocksData.remove(oldBlock, placedBlockKey);
        if (newBlock != null) BlocksData.set(newBlock, placedBlockKey, PersistentDataType.BOOLEAN, true);
    }

    private void onMovedBlocks(List<Block> blocks, BlockFace direction) {
        blocks.stream().filter(block -> BlocksData.has(block, placedBlockKey)).forEach(block -> {
            PistonMoveReaction reaction = block.getPistonMoveReaction();
            onMovedBlock(block, reaction == PistonMoveReaction.BREAK ? null : block.getRelative(direction));
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntity(EntityChangeBlockEvent e) {
        if (e.getTo() != e.getBlock().getType()) {
            onRemovedBlock(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(BlockExplodeEvent e) {
        onRemovedBlock(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        onRemovedBlock(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFade(BlockFadeEvent e) {
        if (e.getNewState().getType() != e.getBlock().getType()) {
            onRemovedBlock(e.getBlock());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        onRemovedBlocks(e.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent e) {
        onMovedBlocks(e.getBlocks(), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPiston(BlockPistonRetractEvent e) {
        onMovedBlocks(e.getBlocks(), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructure(StructureGrowEvent e) {
        onRemovedBlocks(e.getBlocks().stream().map(BlockState::getBlock).toList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent e) {
        onRemovedBlocks(e.getBlocks().stream().map(BlockState::getBlock).toList());
    }


}
