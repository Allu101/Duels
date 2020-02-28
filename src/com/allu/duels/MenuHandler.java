package com.allu.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.allu.duels.utils.ChallengeCreatedEvent;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelpper;

public class MenuHandler {
	
	private ItemStack queue_join_item;
	
	private Duels duels;
	private ItemHelpper itemHelpper;

	public MenuHandler(Duels duels, ItemHelpper itemHelper) {
		this.duels = duels;
		this.itemHelpper = itemHelper;
		queue_join_item = itemHelpper.createItemWithTitle(Material.IRON_SWORD, "Liity 1v1 jonoon");
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
	
	public void inventoryClickHandler(DuelsPlayer dp, ItemStack is) {
		for(Kit kit : duels.getKits()) {
			if(is.equals(kit.getMenuItem())) {
				ChallengeCreatedEvent event = new ChallengeCreatedEvent(dp, dp.getChallengedPlayer(), kit);
				Bukkit.getServer().getPluginManager().callEvent(event);
				dp.getPlayer().closeInventory();
			}
		}
	}
	
	public void onPlayerInteract(ItemStack itemInHand, Action action) {
		if (itemInHand.equals(queue_join_item) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
			
		}
		
	}
	
	public void setLobbyItems(Player p) {
		p.getInventory().clear();
		PlayerInventory pInv = p.getInventory();
		pInv.setItem(0, queue_join_item);
	}
	
}
