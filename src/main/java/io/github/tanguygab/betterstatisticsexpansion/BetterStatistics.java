package io.github.tanguygab.betterstatisticsexpansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class BetterStatistics extends PlaceholderExpansion implements Taskable {

    public File file;
    public FileConfiguration config;
    private StatListener listener;
    private final List<String> placeholders = new ArrayList<>();

    public BetterStatistics() {
        List<String> placeholders = List.of(
                "<statistic>",
                "<statistic>_current",
                "<statistic>_in_<world>",
                "deaths",
                "kills",
                "bred.<animal>", "bred.*",
                "smelt.<item>", "smelt.*"
        );

        placeholders.forEach(placeholder -> this.placeholders.add("%"+getIdentifier()+"_"+placeholder+"%"));
    }

    @Override
    public @NotNull String getIdentifier() {
        return "betterstatistics";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Tanguygab";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public void start() {
        file = new File(getPlaceholderAPI().getDataFolder(), "better-statistics.yml");
        if (!file.exists()) {
            try { //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {throw new RuntimeException(e);}
        }
        config = new YamlConfiguration();
        try {config.load(file);}
        catch (Exception e) {throw new RuntimeException(e);}

        getPlaceholderAPI().getServer().getPluginManager().registerEvents(listener = new StatListener(this), getPlaceholderAPI());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        try {config.save(file);}
        catch (IOException e) {throw new RuntimeException(e);}
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.endsWith("_current") && player.getPlayer() != null) {
            String type = params.substring(0, params.length()-8);

            return getValue(player, type, player.getPlayer().getWorld().getName());
        }
        if (params.contains("_in_")) {
            String[] args = params.split("_in_");
            if (args.length < 2) return null;

            return getValue(player, args[0], args[1]);
        }

        return getValue(player, params, null);
    }

    public String getValue(OfflinePlayer player, String type, String world) {
        return String.valueOf(getInt(player, type, world));
    }

    public int getInt(OfflinePlayer player, String type, String world) {
        String path = player.getUniqueId()+"."+type;
        if (world == null) {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section == null) return 0;
            int value = 0;
            for (String w : section.getKeys(false)) {
                value += section.getInt(w);
            }
            return value;
        }
        return config.getInt(path+"."+world,0);
    }

    public void setValue(OfflinePlayer player, String type, String world, int value) {
        config.set(player.getUniqueId()+"."+type+"."+world, value);
    }

    public void incValue(OfflinePlayer player, String type, World world, int inc) {
        setValue(player, type, world.getName(), getInt(player, type, world.getName()) + inc);
    }
    public void incValue(OfflinePlayer player, String type, World world) {
        incValue(player, type, world, 1);
    }


}
