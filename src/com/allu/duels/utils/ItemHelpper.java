package com.allu.duels.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class ItemHelpper {

	private final static String SKULL_URL_PREFIX = "http://textures.minecraft.net/texture/";

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
	
	public ItemStack createItemWithTitle(Material itemType, String title, Color color) {
		ItemStack is = new ItemStack(itemType, 1);
		LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
		meta.setDisplayName(title);
		meta.setColor(color);
		is.setItemMeta(meta);
		return is;
	}
	
	public static ItemStack getSkull(String skinURL, String name, String... lore) {
        ItemStack head = new ItemStack(Material.SKULL, 1);
        if(skinURL.isEmpty()) {
        	return head;
        }
       
        ItemMeta headMeta = head.getItemMeta();
        headMeta.setDisplayName(name);
        
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
		if (lore.length > 0) {
			headMeta.setLore(Arrays.asList(lore));
		}
        
        head.setItemMeta(headMeta);
        return head;
    }
	
	public static boolean matches(ItemStack is1, ItemStack is2) {
		return is1.getType().equals(is2.getType())
				&& is1.getItemMeta().getDisplayName().equals(is2.getItemMeta().getDisplayName())
				&& loreMatches(is1, is2);
	}
	
	private static boolean loreMatches(ItemStack is1, ItemStack is2) {
		if (is1.getItemMeta().getLore() == null || is2.getItemMeta().getLore() == null)
			return true;
		return is1.getItemMeta().getLore().equals(is2.getItemMeta().getLore());
		
	}
}
