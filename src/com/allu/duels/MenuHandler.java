package com.allu.duels;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.allu.duels.utils.ChallengeCreatedEvent;
import com.allu.duels.utils.Kit;

public class MenuHandler {
	
	private ItemStack queue_join_item;
	
	private Duels duels;

	public MenuHandler(Duels duels) {
		this.duels = duels;
		queue_join_item = createItemWithTitle(Material.IRON_SWORD, "Liity 2v2 jonoon");
	}
	
	public ItemStack createItemWithTitle(Material itemType, String title, String... lore) {
		ItemStack is = new ItemStack(itemType, 1);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(title);
		
		if (lore.length > 0) {
			meta.setLore(Arrays.asList(lore));
		}
		
		is.setItemMeta(meta);
		return is;
	}
	
	public void inventoryClickHandler(DuelsPlayer dp, ItemStack is) {
		if(is.equals(queue_join_item)) {
			dp.getPlayer().sendMessage("T‰st‰ ei viel‰ tapahdu mit‰‰n. :p");
			return;
		}
		for(Kit kit : duels.getKits()) {
			if(is.equals(kit.getMenuItem())) {
				ChallengeCreatedEvent event = new ChallengeCreatedEvent(dp, dp.getChallengedPlayer(), kit);
				Bukkit.getServer().getPluginManager().callEvent(event);
				dp.getPlayer().closeInventory();
			}
		}
	}
	
	public Inventory createKitMenu() {
		Inventory inv = Bukkit.getServer().createInventory(null, 36, ChatColor.BLUE + "" + ChatColor.BOLD + "Valise kitti");
		int i = 10;
		for(Kit kit : duels.getKits()) {
			if(i % 8 == 0) {
				i += 2;
			}
			inv.setItem(i, kit.getMenuItem());
			i++;
		}
		return inv;
	}
	
	public void setLobbyItems(Player p) {
		p.getInventory().clear();
		PlayerInventory pInv = p.getInventory();
		pInv.setItem(0, queue_join_item);
	}
	
}
