package com.allu.duels.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Kit {
	
	private ItemStack menuItem;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	
	public Kit(ItemStack menuItem, List<ItemStack> kitItems) {
		this.menuItem = menuItem;
		this.items = kitItems;
	}

	public ItemStack getMenuItem() {
		return menuItem;
	}

	public List<ItemStack> getItems() {
		return items;
	}
	
}
