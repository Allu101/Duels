package com.allu.duels;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.allu.duels.utils.Gamemode;
import com.allu.minigameapi.nameManager.PrefixHandler;

import net.md_5.bungee.api.ChatColor;

public class Lobby {
	
	public ArrayList<DuelsPlayer> lobbyPlayers = new ArrayList<DuelsPlayer>();
	public String LINE = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "                                                                            ";
	
	private ArrayList<DuelsGame> games = new ArrayList<DuelsGame>();
	private Location spawnLocation;
	
	private MenuHandler menuHandler;

	public Lobby(FileConfiguration config, MenuHandler menuHandler) {
		spawnLocation = new Location(Bukkit.getWorld(config.getString("lobbyworldname")), config.getInt("spawnloc.x"), config.getInt("spawnloc.y"), config.getInt("spawnloc.z"), 
				config.getInt("spawnloc.yaw"), config.getInt("spawnloc.pitch"));
		this.menuHandler = menuHandler;
	}
	
	public void addGame(DuelsGame game) {
		games.add(game);
	}
	
	public boolean isLobbyWorld(Player p) {
		return p.getWorld().getName().equals(Duels.getLobbyWorldName());
	}
	
	public DuelsPlayer getDuelsPlayer(Player p) {
		for(DuelsPlayer dp : lobbyPlayers) {
			if(dp.is(p.getUniqueId().toString())) {
				return dp;
			}
		}
		return null;
	}
	
	public DuelsGame getFreeGame(Gamemode gamemode) {
		for(DuelsGame game : games) {
			if(game.getGamemode().equals(gamemode) && game.isFree()) {
				return game;
			}
		}
		return null;
	}
	
	public void onPlayerJoin(Player p) {
		DuelsPlayer dp = new DuelsPlayer(p);
		lobbyPlayers.add(dp);
		teleportToSpawn(p);
		PrefixHandler.setChatPrefix(p, ChatColor.GOLD + "[1✧]");
	}
	
	public void onPlayerLeave(DuelsPlayer dp) {	
		lobbyPlayers.remove(dp);
	}
	
	public void teleportToSpawn(Player p) {
		p.teleport(spawnLocation);
		p.setGameMode(GameMode.ADVENTURE);
		menuHandler.setLobbyItems(p);
	}
}
