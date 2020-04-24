package com.allu.duels.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class Kit {
	
	private ItemStack menuItem;
	private List<ItemStack> items = new ArrayList<ItemStack>();
	private String name;
	private String arenaType;
	private boolean invulnerable;
	
	public Kit(ItemStack menuItem, List<ItemStack> kitItems, String name, String arenaType) {
		this(menuItem, kitItems, name, arenaType, false);
	}
	public Kit(ItemStack menuItem, List<ItemStack> kitItems, String name, String arenaType, boolean invulnerable) {
		this.menuItem = menuItem;
		this.items = kitItems;
		this.name = name;
		this.arenaType = arenaType;
		this.invulnerable = invulnerable;
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
	
	public boolean isInvulnerable() {
		return invulnerable;
	}
}
