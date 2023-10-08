package me.gibson.itemconverterplugin;

import com.google.gson.Gson;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ItemConverterPlugin extends JavaPlugin implements CommandExecutor {

    public static String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "ItemConverter" + ChatColor.GRAY + "] " + ChatColor.GRAY;
    private Map<Integer, Integer> mapping;

    @Override
    public void onEnable() {
        System.out.println("ItemConverterPlugin enabled");
        getCommand("convertItems").setExecutor(this);
        try {
            loadMapping();
        } catch (IOException e) {
            getLogger().severe("Failed to load mapping.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix+"This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Inventory inventory = player.getInventory();
        boolean itemsConverted = convertInventory(inventory);

        if (itemsConverted) {
            sender.sendMessage(prefix+"Your items have been converted successfully!");
        } else {
            sender.sendMessage(prefix+"No items available for conversion.");
        }

        return true;
    }

    private boolean convertInventory(Inventory inventory) {
        boolean itemsConverted = false;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable && !meta.hasCustomModelData()) {
                    Damageable damageable = (Damageable) meta;
                    int oldDurability = damageable.getDamage();
                    Integer newCustomModelData = mapping.get(oldDurability);
                    if (newCustomModelData != null) {
                        meta.setCustomModelData(newCustomModelData);
                        item.setItemMeta(meta);
                        itemsConverted = true;
                    }
                }
            }
        }
        return itemsConverted;
    }

    private void loadMapping() throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(getDataFolder().getPath(), "mapping.json")));
        Map<String, Object> jsonMapping = new Gson().fromJson(content, Map.class);
        mapping = new HashMap<>();
        for (Map.Entry<String, Object> entry : jsonMapping.entrySet()) {
            mapping.put(Integer.valueOf(entry.getKey()), ((Double) entry.getValue()).intValue());
        }
    }
}
