package fr.communaywen.core.dreamdim.listeners;

import dev.lone.itemsadder.api.CustomStack;
import fr.communaywen.core.AywenCraftPlugin;
import fr.communaywen.core.dreamdim.AdvancementRegister;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CloudSoup implements Listener {
    HashMap<UUID, Integer> cooldown = new HashMap<>();
    AywenCraftPlugin plugin;
    AdvancementRegister register;

    public CloudSoup(AywenCraftPlugin plugin, AdvancementRegister register) {
        this.plugin = plugin;
        this.register = register;
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        CustomStack customStack = CustomStack.byItemStack(event.getItem());
        Player player = event.getPlayer();
        UUID playeruuid = player.getUniqueId();

        if (customStack == null) { return; }
        if (customStack.getNamespacedID().equals("aywen:cloud_soup")) {
            register.grantAdvancement(player, "aywen:leave_earth");

            if (cooldown.containsKey(player.getUniqueId())) {
                cooldown.put(playeruuid, cooldown.get(playeruuid) + 300);
                int minutes_timeleft = cooldown.get(playeruuid) / 60;
                int seconds_timeleft = cooldown.get(playeruuid) % 60;
                player.sendMessage("§aVous pouvez voler pendant 5 minutes de plus ("+minutes_timeleft+"min "+seconds_timeleft+"sec)");
            } else {
                player.sendMessage("§aVous pouvez voler pendant 5 minutes.");
                startTimer(player);
                event.setReplacement(new ItemStack(Material.BOWL, 1));
            }
        }
    }

    public void close() {
        for (UUID playeruuid : cooldown.keySet()) {
            plugin.getServer().getOfflinePlayer(playeruuid).setAllowFlight(false);
        }
    }

    public void startTimer(Player player) {
        UUID playeruuid = player.getUniqueId();
        cooldown.put(playeruuid, 300); //300s = 5 minutes
        player.setAllowFlight(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    return;
                }

                if (cooldown.containsKey(playeruuid) && cooldown.get(playeruuid) > 0) {
                    cooldown.put(playeruuid, cooldown.get(playeruuid) - 1);

                    if (cooldown.get(playeruuid) == 60) {
                        player.sendMessage("§aIl vous reste 1 minute de vol");
                    } else if (cooldown.get(playeruuid) == 30) {
                        player.sendMessage("§aIl vous reste 30 secondes de vol");
                    } else if (cooldown.get(playeruuid) == 10) {
                        player.sendMessage("§aIl vous reste 10 secondes de vol");
                    } else if (cooldown.get(playeruuid) == 3) {
                        player.sendMessage("§aIl vous reste 3 secondes de vol");
                    } else if (cooldown.get(playeruuid) == 2) {
                        player.sendMessage("§aIl vous reste 2 secondes de vol");
                    } else if (cooldown.get(playeruuid) == 1) {
                        player.sendMessage("§aIl vous reste 1 secondes de vol");
                    }
                } else {
                    player.sendMessage("§cVotre soupe de nuage s'est épuisée");
                    cooldown.remove(playeruuid);
                    player.setAllowFlight(false);
                    cancel();
                }
            }
        }.runTaskLater(this.plugin, 20);
    }
}
