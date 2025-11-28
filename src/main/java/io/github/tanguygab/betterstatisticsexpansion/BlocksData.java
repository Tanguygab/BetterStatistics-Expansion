package io.github.tanguygab.betterstatisticsexpansion;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;

public class BlocksData {

    public static <P, C> void set(
            Block block,
            NamespacedKey key,
            PersistentDataType<P, C> dataType,
            C value
    ) {
        NamespacedKey newKey = createKey(block, key);
        block.getChunk().getPersistentDataContainer().set(newKey, dataType, value);
    }

    public static boolean has(Block block, NamespacedKey key) {
        return block.getChunk().getPersistentDataContainer().has(createKey(block, key));
    }
    public static void remove(Block block, NamespacedKey key) {
        block.getChunk().getPersistentDataContainer().remove(createKey(block, key));
    }

    private static NamespacedKey createKey(Block block, NamespacedKey key) {
        String namespace = key.getNamespace();
        String keyValue = key.getKey();

        String newKeyValue = keyValue + "_" + (block.getX() - block.getChunk().getX()) + "-" + block.getY() + "-" + (block.getZ() - block.getChunk().getZ());
        //noinspection UnstableApiUsage
        return new NamespacedKey(namespace, newKeyValue);
    }
}
