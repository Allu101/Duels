package com.allu.duels.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Kit {
	
	private ItemStack menuItem;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	private String name;
	private String arenaType;
	

	public Kit(ItemStack menuItem, List<ItemStack> kitItems, String name, String arenaType) {
		this.menuItem = menuItem;
		this.items = kitItems;
		this.name = name;
		this.arenaType = arenaType;
	}
	
	public ItemStack getMenuItem() {
		return menuItem;
	}

	public List<ItemStack> getItems() {
		return items;
	}
	
	public String getName() {
		return name;
	}
	
	public String getArenaType() {
		return arenaType;
	}
}
