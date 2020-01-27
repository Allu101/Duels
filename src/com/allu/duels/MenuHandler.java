package com.allu.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.allu.duels.utils.ChallengeCreatedEvent;
import com.allu.duels.utils.ItemHelpper;
import com.allu.duels.utils.Kit;

public class MenuHandler {
	
	private ItemStack queue_join_item;
	
	private Duels duels;
	private ItemHelpper itemHelpper;

	public MenuHandler(Duels duels) {
		this.duels = duels;
		itemHelpper = new ItemHelpper();
		queue_join_item = itemHelpper.createItemWithTitle(Material.IRON_SWORD, "Liity 2v2 jonoon");
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
			inv.setItem(i, itemHelpper.createItemWithTitle(kit.getMenuItem().getType(), kit.getName() + " Duel"
					, ChatColor.YELLOW + "Klikkaa liitty‰ksesi."));
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
