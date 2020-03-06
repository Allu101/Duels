package com.allu.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelpper;

public class MenuHandler {
	
	private ItemStack queueJoinItem;
	private ItemStack challengeItem;
	
	private Duels duels;
	private ItemHelpper itemHelpper;

	public MenuHandler(Duels duels, ItemHelpper itemHelper) {
		this.duels = duels;
		this.itemHelpper = itemHelper;
		queueJoinItem = itemHelpper.createItemWithTitle(Material.IRON_SWORD, "§aPelaa kilpailullinen peli");
		challengeItem = itemHelpper.createItemWithTitle(Material.WOOD_SWORD, "§aHaasta kaveri lyömällä!");
	}
	
	public Inventory createKitMenu() {
		Inventory inv = Bukkit.getServer().createInventory(null, 36, ChatColor.BLUE + "" + ChatColor.BOLD + "Valise kitti");
		int i = 10;
		for(Kit kit : duels.getKits()) {
			if(i == 17 || i == 26) {
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
				duels.getLobby().createNewChallenge(dp, dp.getChallengedPlayer(), kit);
				dp.getPlayer().closeInventory();
			}
		}
	}
	
	public void onPlayerInteract(ItemStack itemInHand, Action action) {
		if (itemInHand.equals(queueJoinItem) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
			
		}
	}
	
	public void setLobbyItems(Player p) {
		p.getInventory().clear();
		PlayerInventory pInv = p.getInventory();
		pInv.setItem(0, challengeItem);
		pInv.setItem(1, queueJoinItem);
	}
	
	public ItemStack getQueueItem() {
		return this.queueJoinItem;
	}
	
	public ItemStack getChallengeItem() {
		return this.challengeItem;
	}
	
}
