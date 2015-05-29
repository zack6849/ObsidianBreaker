package com.zack6849.obsidianbreaker;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Zack on 1/8/2015.
 */
public class Main extends JavaPlugin implements Listener {
    private Map<Material, HashMap<String, Integer>> data = new HashMap<Material, HashMap<String, Integer>>();

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        saveDefaultConfig();
        data.put(Material.BEDROCK, new HashMap<String, Integer>());
        data.get(Material.BEDROCK).put("durability", getConfig().getInt("durability.bedrock"));
        data.put(Material.OBSIDIAN, new HashMap<String, Integer>());
        data.get(Material.OBSIDIAN).put("durability", getConfig().getInt("durability.obsidian"));
        getServer().getPluginManager().registerEvents(this, this);
        reloadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("obreaker")){
            if(args.length == 1){
                if(args[0].equalsIgnoreCase("reload")){
                    reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                }
            }else{
                sender.sendMessage(ChatColor.YELLOW + "ObsidianDestroyer");
            }
        }
        return false;
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.getLocation().getBlock().getType() == Material.WATER || event.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
            return;
        }
        int r = getConfig().getInt("radius");
        Location playerPos = event.getLocation();
        for (int x = (r * -1); x <= r; x++) {
            for (int y = (r * -1); y <= r; y++) {
                for (int z = (r * -1); z <= r; z++) {
                    Block b = playerPos.getWorld().getBlockAt(playerPos.getBlockX() + x, playerPos.getBlockY() + y, playerPos.getBlockZ() + z);
                    if (data.containsKey(b.getType())) {
                        int resistance = data.get(b.getType()).get("durability");
                        if (b.hasMetadata("hits")) {
                            b.setMetadata("hits", new FixedMetadataValue(this, b.getMetadata("hits").get(0).asInt() + 1));
                            if (b.getMetadata("hits").get(0).asInt() >= resistance) {
                                b.removeMetadata("hits", this);
                                b.setType(Material.AIR);
                                event.getLocation().getWorld().playEffect(event.getLocation(), Effect.ENDER_SIGNAL, 1);
                            }
                        } else {
                            b.setMetadata("hits", new FixedMetadataValue(this, 1));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onActionPerformed(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.POTATO_ITEM) {
            if (data.containsKey(event.getClickedBlock().getType())) {
                int durability = data.get(event.getClickedBlock().getType()).get("durability");
                if (event.getClickedBlock().hasMetadata("hits")) {
                    int hits = event.getClickedBlock().getMetadata("hits").get(0).asInt();
                    event.getPlayer().sendMessage(String.format(ChatColor.GRAY + "This blocks durability is currently " + ChatColor.LIGHT_PURPLE + "%s/%s", durability - hits, durability));
                } else {
                    event.getPlayer().sendMessage(String.format(ChatColor.GRAY + "This blocks durability is currently " + ChatColor.LIGHT_PURPLE + "%s/%s", durability, durability));
                }
            }
        }
    }
}
