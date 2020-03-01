package com.allu.duels;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import com.allu.duels.utils.Gamemode;

import net.md_5.bungee.api.ChatColor;

public class Lobby {
	
	public String LINE = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "                                                                            ";
	
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
	private ArrayList<DuelsGame> games = new ArrayList<DuelsGame>();
	private Location spawnLocation;
	
	private MenuHandler menuHandler;

	public Lobby(FileConfiguration config, MenuHandler menuHandler) {
		spawnLocation = new Location(Bukkit.getWorld(config.getString("lobbyworldname")), config.getDouble("spawnloc.x"), config.getDouble("spawnloc.y"), config.getDouble("spawnloc.z"), 
				config.getInt("spawnloc.yaw"), config.getInt("spawnloc.pitch"));
		this.menuHandler = menuHandler;
	}
	
	public void addGame(DuelsGame game) {
		games.add(game);
	}
	
	public DuelsGame getFreeGame(Gamemode gamemode) {
		for(DuelsGame game : games) {
			if(game.getGamemode().equals(gamemode) && game.isFree()) {
				return game;
			}
		}
		return null;
	}
	
	public DuelsPlayer getDuelsPlayer(Player p) {
		for(DuelsPlayer dp : players) {
			if(dp.is(p.getUniqueId().toString())) {
				return dp;
			}
		}
		return null;
	}
	
	public boolean isLobbyWorld(Entity e) {
		return e.getWorld().getName().equals(Duels.getLobbyWorldName());
	}
	
	public void onPlayerJoin(Player p) {
		DuelsPlayer dp = new DuelsPlayer(p, new PlayerSidebarHandler());
		Duels.plugin.dbHandler.loadStatsSQL(dp);
		sendPlayerToLobby(dp);
	}
	
	public void onPlayerLeave(DuelsPlayer dp) {
		players.remove(dp);
	}
	
	/**
	 * This method should be used for returning players back to lobby!
	 * @param dp
	 */
	public void sendPlayerToLobby(DuelsPlayer dp) {
		
		System.out.println("Sending " + dp.getPlayer().getName() + " to lobby...");
		
		dp.setGameWhereJoined(null);
		
		dp.getPlayer().setScoreboard(dp.getSidebarHandler().getLobbyBoard());
		dp.getSidebarHandler().updateLobbySidebarWinsAndWinStreaks(
				dp.getWins(), dp.getCurrentWinStreak(), dp.getBestWinStreak(), dp.getPlayedGames(), dp.getEloScore());
		
		teleportToSpawn(dp.getPlayer());
		
		boolean playerFound = false;
		
		for (DuelsPlayer dp2 : players) {
			if (dp2.is(dp.getPlayer().getUniqueId().toString())) {
				playerFound = true;
				dp2.setWins(dp.getWins());
				dp2.setBestWinStreak(dp.getBestWinStreak());
				dp2.setCurrentWinStreak(dp.getCurrentWinStreak());
				dp2.setEloScore(dp.getEloScore());
				dp2.setPlayedGames(dp.getPlayedGames());
			}
		}
		
		if (!playerFound)
			players.add(dp);
	}
	
	public void teleportToSpawn(Player p) {
		p.teleport(spawnLocation, TeleportCause.PLUGIN);
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20);
		clearPlayerInventoryAndEquipment(p);
		menuHandler.setLobbyItems(p);
	}
	
	public void setKitItems(Player p, List<ItemStack> items) {
		p.getInventory().clear();
		for (ItemStack is : items) {
			String itemTypeString = is.getType().toString();
			if (itemTypeString.contains("HELMET")) {
				p.getInventory().setHelmet(new ItemStack(is));
			}
			else if (itemTypeString.contains("LEGGINGS")) {
				p.getInventory().setLeggings(new ItemStack(is));
			}
			else if (itemTypeString.contains("CHESTPLATE")) {
				p.getInventory().setChestplate(new ItemStack(is));
			}
			else if (itemTypeString.contains("_BOOTS")) {
				p.getInventory().setBoots(new ItemStack(is));
			}
			else {
				p.getInventory().addItem(new ItemStack(is));
			}
		}
		
		p.updateInventory();
	}
	
	public void clearPlayerInventoryAndEquipment(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.getInventory().setChestplate(null);
		player.getInventory().setLeggings(null);
		player.getInventory().setBoots(null);
	}
	
	public void clearPotionEffect(Player p) {
		for(PotionEffect effect : p.getActivePotionEffects()) {
		    p.removePotionEffect(effect.getType());
		}
	}
}
