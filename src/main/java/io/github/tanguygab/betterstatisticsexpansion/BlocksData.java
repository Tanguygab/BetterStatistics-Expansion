package io.github.tanguygab.betterstatisticsexpansion;

public class BlocksData {


    public static Object getPersistentData(
            Block block,
            NamespacedKey key,
            PersistentDataType<?, ?> dataType
    ) {
        NamespacedKey newKey = createKey(block, key);
        return chunk.persistentDataContainer.get(block, newKey, dataType);
    }

    public static <P, C> void setPersistentData(
            Block block,
            NamespacedKey key,
            PersistentDataType<P, C> dataType,
            C value
    ) {
        NamespacedKey newKey = createKey(block, key);
        chunk.persistentDataContainer.set(newKey, dataType, value);
    }

    public static boolean hasPersistentData(Block block, NamespacedKey key) {
        return chunk.persistentDataContainer.has(createKey(key));
    }

    private NamespacedKey createKey(Block block, NamespacedKey key) {
        val namespace = key.namespace;
        val keyValue = key.key;

        val newKeyValue = keyValue + "," + (block.getX() - block.getChunk().getX()) + "," + (block.getY() - block.getChunk().getY()) + "," + (block.getZ() - block.getChunk().getZ());

        return NamespacedKey(namespace, newKeyValue);
    }
}
