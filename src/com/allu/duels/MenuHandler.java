package com.allu.duels;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.allu.duels.utils.ItemHelpper;
import com.allu.duels.utils.Kit;

public class MenuHandler {
	
	private ItemStack queue_join_item;
	
	private ItemHelpper itemHelpper;
	private Duels duels;

	public MenuHandler(Duels duels) {
		this.duels = duels;
		itemHelpper = new ItemHelpper();
		queue_join_item = itemHelpper.createItemWithTitle(Material.IRON_SWORD, "Liity 2v2 jonoon");
	}
	
	public void inventoryClickHandler(Player p, InventoryClickEvent e) {
		if(e.getClick().isKeyboardClick() && !p.getWorld().getName().equals(Duels.getLobbyWorldName())) {
			return;
		}
		e.setCancelled(true);
		handleInventoryClick(e.getCurrentItem());
	}
	
	public Inventory createKitMenu(Player p) {
		Inventory inv = p.getServer().createInventory(null, 36, ChatColor.BLUE + "" + ChatColor.BOLD + "Valise kitti");
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
	
	private void handleInventoryClick(ItemStack is) {
		if(is.equals(queue_join_item)) {
			
		}
		for(Kit duel : duels.getKits()) {
			if(is.equals(duel.getMenuItem())) {
				
			}
		}
	}
	
	private void getKitToPlayer(Player p) {
		
	}
}
