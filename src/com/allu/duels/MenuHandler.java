package com.allu.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.allu.duels.utils.Kit;
import com.allu.minigameapi.ItemHelper;

public class MenuHandler {
	
	private ItemStack queueJoinItem;
	private ItemStack challengeItem;
	private ItemStack exitQueueItem;
	
	private Duels duels;

	public MenuHandler(Duels duels, ItemHelper itemHelper) {
		this.duels = duels;
		queueJoinItem = itemHelper.createItemWithTitle(Material.IRON_SWORD, "§aPelaa kilpailullinen peli!");
		challengeItem = itemHelper.createItemWithTitle(Material.WOOD_SWORD, "§aHaasta kaveri lyömällä!");
		exitQueueItem = itemHelper.createItemWithTitle(Material.BARRIER, "§cPoistu jonosta");
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
				if (dp.getChallengedPlayers() != null) {
					duels.getLobby().createNewChallenge(dp, dp.getChallengedPlayers(), kit);
				}
				dp.getPlayer().closeInventory();
			}
		}
	}
	
	public void setLobbyItems(Player p) {
		p.getInventory().clear();
		PlayerInventory pInv = p.getInventory();
		pInv.setItem(0, challengeItem);
		pInv.setItem(1, queueJoinItem);
	}
	
	public void addExitQueueItemToPlayer(Player p) {
		p.getInventory().setItem(2, this.exitQueueItem);
	}
	public void removeExitQueueItemFromPlayer(Player p) {
		p.getInventory().remove(this.exitQueueItem);
	}
	
	public ItemStack getQueueItem() {
		return this.queueJoinItem;
	}
	
	public ItemStack getChallengeItem() {
		return this.challengeItem;
	}
	
	public ItemStack getExitQueueItem() {
		return this.exitQueueItem;
	}
	
}
