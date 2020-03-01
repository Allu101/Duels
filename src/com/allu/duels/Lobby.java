package com.allu.duels;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.allu.duels.utils.Gamemode;
import net.md_5.bungee.api.ChatColor;

public class Lobby {
	
	public String LINE = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "                                                                            ";
	
	private ArrayList<DuelsPlayer> players = new ArrayList<DuelsPlayer>();
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
		players.add(dp);
		teleportToSpawn(p);
		p.setScoreboard(dp.getSidebarHandler().getLobbyBoard());
		dp.getSidebarHandler().updateLobbySidebarWinsAndWinStreaks(
				dp.getWins(), dp.getCurrentWinStreak(), dp.getBestWinStreak(), dp.getPlayedGames());
	}
	
	public void onPlayerLeave(DuelsPlayer dp) {
		players.remove(dp);
	}
	
	public void teleportToSpawn(Player p) {
		p.teleport(spawnLocation);
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20);
		menuHandler.setLobbyItems(p);
	}
	
}
